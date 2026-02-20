package com.greedy.meetlink.participant.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByMeetingAndNickname(Meeting meeting, String nickname);

    Optional<Participant> findByMeeting_CodeAndToken(String meetingCode, String token);

    List<Participant> findByMeeting(Meeting meeting);
}
