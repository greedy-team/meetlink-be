package com.greedy.meetlink.candidate.repository;

import com.greedy.meetlink.candidate.entity.TimeCandidate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeCandidateRepository extends JpaRepository<TimeCandidate, Long> {

    /** 모임 ID로 모든 TimeCandidate 조회 (rank 순으로 정렬) */
    List<TimeCandidate> findByMeetingIdOrderByRankAsc(Long meetingId);

    /** 모임 코드로 모든 TimeCandidate 조회 (추천 순위 리스트용) */
    @Query(
            "SELECT tc FROM TimeCandidate tc "
                    + "JOIN FETCH tc.meeting m "
                    + "WHERE m.code = :code "
                    + "ORDER BY tc.rank ASC")
    List<TimeCandidate> findByMeetingCodeOrderByRank(@Param("code") String code);

    // 모임 시간 추천 결과가 마지막으로 생성된 시간 조회
    @Query(
            "SELECT MAX(tc.createdAt) FROM TimeCandidate tc "
                    + "JOIN tc.meeting m "
                    + "WHERE m.code = :code")
    Optional<LocalDateTime> findLatestCreatedAtByMeetingCode(@Param("code") String code);

    // 재계산 시 기존 결과 초기화
    @Modifying
    @Query("DELETE FROM TimeCandidate tc WHERE tc.meeting.code = :code")
    void deleteByMeetingCode(@Param("code") String code);

    // 순위 순서대로 가져오기
    @Query(
            "SELECT tc FROM TimeCandidate tc "
                    + "JOIN tc.meeting m "
                    + "WHERE m.code = :code "
                    + "ORDER BY tc.rank ASC")
    List<TimeCandidate> findByMeetingCode(@Param("code") String code);
}
