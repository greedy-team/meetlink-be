package com.greedy.meetlink.participant.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByMeetingAndNickname(Meeting meeting, String nickname);

    List<Participant> findByMeeting(Meeting meeting);

    Optional<Participant> findByMeetingAndToken(Meeting meeting, String token);

    @Query(
            """
        SELECT MAX(p.timeSubmittedAt)
        FROM Participant  p
        JOIN p.meeting m
        WHERE m.code = :code
    """)
    Optional<LocalDateTime> findLastTimeSubmission(String code);

    @Modifying
    @Query(
            """
        UPDATE Participant p SET p.timeSubmittedAt = null
        WHERE p.meeting = :meeting
          AND p.timeSubmittedAt IS NOT NULL
          AND NOT EXISTS (
              SELECT 1 FROM TimeAvailability ta
              WHERE ta.meeting = :meeting AND ta.participant = p
          )
    """)
    void resetTimeSubmittedIfNoAvailability(Meeting meeting);
}
