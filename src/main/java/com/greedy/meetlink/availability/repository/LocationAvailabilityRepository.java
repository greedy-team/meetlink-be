package com.greedy.meetlink.availability.repository;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationAvailabilityRepository extends JpaRepository<LocationAvailability, Long> {
    boolean existsByParticipant(Participant participant);

    @Query(
            """
        select distinct la.participant.id
        from LocationAvailability la
        join la.participant p
        where p.meeting = :meeting
    """)
    List<Long> findSubmittedParticipantIds(Meeting meeting);

    Optional<LocationAvailability> findByParticipant(Participant participant);

    @Query(
            "SELECT la FROM LocationAvailability la JOIN FETCH la.participant WHERE la.participant IN :participants")
    List<LocationAvailability> findByParticipantIn(
            @Param("participants") List<Participant> participants);

    /**
     * 모임 코드로 해당 모임 참여자들의 출발지 정보 전체 조회
     *
     * <p>LocationAvailability → Participant → Meeting 경로로 조인 Participant를 Fetch Join하여 service에서
     * getParticipant() 호출 시 추가 쿼리 방지 장소 추천 알고리즘(PlaceRecommendationService)에서 사용
     */
    @Query(
            """
        SELECT la FROM LocationAvailability la
        JOIN FETCH la.participant p
        JOIN p.meeting m
        WHERE m.code = :code
    """)
    List<LocationAvailability> findByMeetingCode(@Param("code") String code);
}
