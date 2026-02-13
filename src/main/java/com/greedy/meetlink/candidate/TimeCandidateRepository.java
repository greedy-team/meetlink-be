package com.greedy.meetlink.candidate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimeCandidateRepository extends JpaRepository<TimeCandidate, Long> {

    /**
     * 모임 ID로 모든 TimeCandidate 조회 (rank 순으로 정렬)
     */
    List<TimeCandidate> findByMeetingIdOrderByRankAsc(Long meetingId);

    /**
     * 모임 코드로 모든 TimeCandidate 조회 (추천 순위 리스트용)
     */
    @Query("SELECT tc FROM TimeCandidate tc " +
            "JOIN FETCH tc.meeting m " +
            "WHERE m.code = :code " +
            "ORDER BY tc.rank ASC")
    List<TimeCandidate> findByMeetingCodeOrderByRank(@Param("code") String code);
}
