package com.greedy.meetlink.availability.repository;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TimeAvailabilityRepository extends JpaRepository<TimeAvailability, Long> {
    boolean existsByMeetingAndParticipant(Meeting meeting, Participant participant);

    @Query(
            """
        select distinct ta.participant.id
        from TimeAvailability ta
        where ta.meeting = :meeting
    """)
    List<Long> findSubmittedParticipantIds(Meeting meeting);

    void deleteByMeetingAndParticipant(Meeting meeting, Participant participant);
}
