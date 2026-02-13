package com.greedy.meetlink.result.repository;

import com.greedy.meetlink.result.entity.MeetingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingResultRepository extends JpaRepository<MeetingResult, Long> {

    @Query("SELECT mr FROM MeetingResult mr " +
            "JOIN FETCH mr.meeting m " +
            "LEFT JOIN FETCH mr.placeCandidate pc " +
            "LEFT JOIN FETCH pc.travelInfos ti " +    // 1. 장소의 상세 이동 정보들 조인
            "LEFT JOIN FETCH ti.participant p " +      // 2. 이동 정보 안의 참여자 닉네임 조인
            "WHERE m.code = :code")
    Optional<MeetingResult> findByMeetingCode(@Param("code") String code);

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
