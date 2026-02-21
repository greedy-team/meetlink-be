package com.greedy.meetlink.result.repository;

import com.greedy.meetlink.result.entity.MeetingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingResultRepository extends JpaRepository<MeetingResult, Long> {

    /**
     * 모임 코드로 추천 결과 조회
     *
     * [변경 이유]
     * 기존: 4단계 Fetch Join (MeetingResult → PlaceCandidate → travelInfos → Participant)
     *   - 참여자 수가 많아질수록 카테시안 곱 문제로 쿼리 결과 행 폭발 가능성 존재
     *
     * 변경: Meeting, PlaceCandidate까지만 Fetch Join
     *   - travelInfos, Participant는 PlaceCandidate 엔티티에 @BatchSize를 선언하여
     *     IN 절 배치 로딩으로 처리 → 추가 쿼리 2방으로 제어 가능
     *
     * PlaceCandidate 엔티티에 아래 설정 필요:
     *   @OneToMany(mappedBy = "placeCandidate", fetch = FetchType.LAZY)
     *   @BatchSize(size = 100)
     *   private List<PlaceTravelInfo> travelInfos;
     */
    @Query("SELECT mr FROM MeetingResult mr " +
            "JOIN FETCH mr.meeting m " +
            "LEFT JOIN FETCH mr.placeCandidate pc " +
            "WHERE m.code = :code")
    Optional<MeetingResult> findWithPlaceByMeetingCode(@Param("code") String code);

    /**
     * 모임 ID로 추천 결과 조회 (시간 후보, 장소 후보 fetch join)
     */
    @Query("SELECT mr FROM MeetingResult mr " +
            "LEFT JOIN FETCH mr.meeting " +
            "LEFT JOIN FETCH mr.timeCandidate " +
            "LEFT JOIN FETCH mr.placeCandidate " +
            "WHERE mr.meeting.id = :meetingId")
    Optional<MeetingResult> findByMeetingId(@Param("meetingId") Long meetingId);
}
