package com.greedy.meetlink.candidate;

import com.greedy.meetlink.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceCandidateRepository extends JpaRepository<PlaceCandidate, Long> {
    void deleteByMeeting(Meeting meeting);

    List<PlaceCandidate> findByMeeting(Meeting meeting);
}
