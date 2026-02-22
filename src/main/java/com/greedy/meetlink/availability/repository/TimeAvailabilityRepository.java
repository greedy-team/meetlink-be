package com.greedy.meetlink.availability.repository;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.availability.repository.projection.TimeAvailabilityHeatmapRow;
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

    @Query(
            """
        SELECT
            ta.date AS date,
            ta.dayOfWeek AS dayOfWeek,
            ta.startTime AS startTime,
            COUNT(ta) AS availableCount
        FROM TimeAvailability ta
        WHERE ta.meeting.code = :code
        GROUP BY ta.date, ta.dayOfWeek, ta.startTime
        ORDER BY COUNT(ta) DESC,
                ta.date ASC,
                ta.dayOfWeek ASC,
                ta.startTime ASC
    """)
    List<TimeAvailabilityHeatmapRow> findHeatmapByMeetingCode(String code);

    List<TimeAvailability> findByMeetingAndParticipant(Meeting meeting, Participant participant);
}
