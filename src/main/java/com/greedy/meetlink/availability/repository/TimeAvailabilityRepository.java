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

    @Modifying(clearAutomatically = true)
    @Query(
            "DELETE FROM TimeAvailability ta WHERE ta.meeting = :meeting AND ta.participant = :participant")
    void deleteByMeetingAndParticipant(Meeting meeting, Participant participant);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE TimeAvailability ta SET ta.isDeleted = true WHERE ta.meeting = :meeting")
    void deleteByMeeting(Meeting meeting);

    @Modifying(clearAutomatically = true)
    @Query(
            "UPDATE TimeAvailability ta SET ta.isDeleted = true WHERE ta.meeting = :meeting AND ta.startTime < :rangeStart")
    void deleteBeforeRangeStart(Meeting meeting, LocalTime rangeStart);

    @Modifying(clearAutomatically = true)
    @Query(
            "UPDATE TimeAvailability ta SET ta.isDeleted = true WHERE ta.meeting = :meeting AND ta.startTime >= :rangeEnd")
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

    @Modifying(clearAutomatically = true)
    @Query(
            value =
                    "UPDATE time_availability SET is_deleted = false WHERE meeting_id = :meetingId AND day_of_week IS NOT NULL",
            nativeQuery = true)
    void restoreWeeklyDataByMeeting(Long meetingId);

    @Modifying(clearAutomatically = true)
    @Query(
            value =
                    "UPDATE time_availability SET is_deleted = false WHERE meeting_id = :meetingId AND date IS NOT NULL",
            nativeQuery = true)
    void restoreSpecificDateDataByMeeting(Long meetingId);
}
