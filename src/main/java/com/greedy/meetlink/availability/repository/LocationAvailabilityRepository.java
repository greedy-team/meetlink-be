package com.greedy.meetlink.availability.repository;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.participant.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationAvailabilityRepository extends JpaRepository<LocationAvailability, Long> {
    boolean existsByParticipant(Participant participant);
}
