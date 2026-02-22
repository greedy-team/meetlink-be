package com.greedy.meetlink.participant.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
