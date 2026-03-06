package com.greedy.meetlink.availability.repository;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    void deleteByMeeting(Meeting meeting);

    @Modifying
    @Query(
            "DELETE FROM TimeAvailability ta WHERE ta.meeting = :meeting AND ta.startTime < :rangeStart")
    void deleteBeforeRangeStart(Meeting meeting, LocalTime rangeStart);

    @Modifying
    @Query(
            "DELETE FROM TimeAvailability ta WHERE ta.meeting = :meeting AND ta.startTime >= :rangeEnd")
    void deleteFromRangeEnd(Meeting meeting, LocalTime rangeEnd);

    List<TimeAvailability> findByMeeting(Meeting meeting);

    List<TimeAvailability> findByMeetingAndParticipant(Meeting meeting, Participant participant);

    @Query(
            """
        SELECT ta
        FROM TimeAvailability ta
        JOIN FETCH ta.participant
        WHERE ta.meeting.code = :code
          AND ta.startTime >= :rangeStart
          AND ta.startTime < :rangeEnd
        ORDER BY ta.date ASC, ta.dayOfWeek ASC, ta.startTime ASC
    """)
    List<TimeAvailability> findByMeetingCodeInTimeRange(
            String code, LocalTime rangeStart, LocalTime rangeEnd);
}
