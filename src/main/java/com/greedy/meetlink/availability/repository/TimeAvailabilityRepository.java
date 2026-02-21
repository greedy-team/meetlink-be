package com.greedy.meetlink.availability.repository;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeAvailabilityRepository extends JpaRepository<TimeAvailability, Long> {
    boolean existsByMeetingAndParticipant(Meeting meeting, Participant participant);
}
