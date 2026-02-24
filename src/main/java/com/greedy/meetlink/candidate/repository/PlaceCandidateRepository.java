package com.greedy.meetlink.candidate.repository;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.meeting.entity.Meeting;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PlaceCandidateRepository extends JpaRepository<PlaceCandidate, Long> {
    @Query("SELECT MAX(pc.createdAt) FROM PlaceCandidate pc WHERE pc.meeting = :meeting")
    Optional<LocalDateTime> findLastCalculatedAt(Meeting meeting);

    @Modifying
    void deleteByMeeting(Meeting meeting);

    List<PlaceCandidate> findByMeetingOrderByRankAsc(Meeting meeting);
}
