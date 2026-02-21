package com.greedy.meetlink.result.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.result.entity.MeetingResult;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingResultRepository extends JpaRepository<MeetingResult, Long> {
    Optional<MeetingResult> findByMeeting(Meeting meeting);
}
