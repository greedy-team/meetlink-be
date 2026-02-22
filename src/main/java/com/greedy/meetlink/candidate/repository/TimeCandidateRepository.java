package com.greedy.meetlink.candidate.repository;

import com.greedy.meetlink.candidate.entity.TimeCandidate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TimeCandidateRepository extends JpaRepository<TimeCandidate, Long> {
    @Query(
            """
        SELECT MAX(tc.createdAt)
        FROM TimeCandidate tc
        WHERE tc.meeting.code = :code
    """)
    Optional<LocalDateTime> findLastCalculatedAt(String code);

    @Modifying
    @Query("DELETE FROM TimeCandidate tc WHERE tc.meeting.code = :code")
    void deleteByMeetingCode(String code);

    List<TimeCandidate> findByMeeting_CodeOrderByRankAsc(String code);
}
