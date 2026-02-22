package com.greedy.meetlink.candidate.repository;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.meeting.entity.Meeting;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCandidateRepository extends JpaRepository<PlaceCandidate, Long> {
    void deleteByMeeting(Meeting meeting);

    List<PlaceCandidate> findByMeeting(Meeting meeting);

    List<PlaceCandidate> findByMeetingOrderByRankAsc(Meeting meeting);
}
