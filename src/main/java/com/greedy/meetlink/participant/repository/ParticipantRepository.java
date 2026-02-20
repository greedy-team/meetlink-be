package com.greedy.meetlink.participant.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByMeetingAndNickname(Meeting meeting, String nickname);

    List<Participant> findByMeeting(Meeting meeting);

    Optional<Participant> findByMeetingAndToken(Meeting meeting, String token);
}
