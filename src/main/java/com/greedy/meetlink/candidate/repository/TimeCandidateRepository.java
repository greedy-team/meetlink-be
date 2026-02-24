package com.greedy.meetlink.candidate.repository;

import com.greedy.meetlink.candidate.entity.TimeCandidate;
import com.greedy.meetlink.meeting.entity.Meeting;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TimeCandidateRepository extends JpaRepository<TimeCandidate, Long> {
    @Query("SELECT MAX(tc.createdAt) FROM TimeCandidate tc WHERE tc.meeting = :meeting")
    Optional<LocalDateTime> findLastCalculatedAt(Meeting meeting);

    @Modifying
    void deleteByMeeting(Meeting meeting);

    List<TimeCandidate> findByMeetingOrderByRankAsc(Meeting meeting);
}
