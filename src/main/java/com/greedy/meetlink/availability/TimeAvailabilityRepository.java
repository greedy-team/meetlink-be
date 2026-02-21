package com.greedy.meetlink.availability;

import com.greedy.meetlink.participant.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeAvailabilityRepository extends JpaRepository<TimeAvailability, Long> {
    boolean existsByParticipant(Participant participant);
}
