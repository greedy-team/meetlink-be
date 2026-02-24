package com.greedy.meetlink.availability.repository;

import com.greedy.meetlink.availability.entity.LocationAvailability;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LocationAvailabilityRepository extends JpaRepository<LocationAvailability, Long> {
    boolean existsByParticipant(Participant participant);

    @Query(
            """
        select distinct la.participant.id
        from LocationAvailability la
        join la.participant p
        where p.meeting = :meeting
    """)
    List<Long> findSubmittedParticipantIds(Meeting meeting);

    Optional<LocationAvailability> findByParticipant(Participant participant);

    @Query(
            "SELECT la FROM LocationAvailability la JOIN FETCH la.participant WHERE la.participant IN :participants")
    List<LocationAvailability> findByParticipantIn(
            @Param("participants") List<Participant> participants);
}
