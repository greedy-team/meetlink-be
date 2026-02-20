package com.greedy.meetlink.participant.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByMeetingAndNickname(Meeting meeting, String nickname);

    Optional<Participant> findByMeeting_CodeAndToken(String meetingCode, String token);

    @Query(
            """
        select p from Participant p
        left join fetch p.startPoint
        left join fetch p.availableTimes
        where p.meeting = :meeting
    """)
    List<Participant> findWithDetailsByMeeting(@Param("meeting") Meeting meeting);
}
