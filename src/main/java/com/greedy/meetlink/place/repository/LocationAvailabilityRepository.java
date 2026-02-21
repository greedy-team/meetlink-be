package com.greedy.meetlink.place.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationAvailabilityRepository extends JpaRepository<LocationAvailability, Long> {

    /**
     * 모임 코드로 해당 모임 참여자들의 출발지 정보 전체 조회
     *
     * LocationAvailability → Participant → Meeting 경로로 조인
     * Participant를 Fetch Join하여 service에서 participant 접근 시 추가 쿼리 방지
     *
     * @param code 모임 코드
     * @return 해당 모임 참여자들의 LocationAvailability 목록
     */
    @Query("SELECT la FROM LocationAvailability la " +
            "JOIN FETCH la.participant p " +
            "JOIN p.meeting m " +
            "WHERE m.code = :code")
    List<LocationAvailability> findByMeetingCode(@Param("code") String code);
}
