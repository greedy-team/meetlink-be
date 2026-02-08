package com.greedy.meetlink.meeting;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    @EntityGraph(attributePaths = {"participants", "participants.startPoint", "participants.availableTimes", "meetingResult"})
    Optional<Meeting> findByCode(String code);
}
