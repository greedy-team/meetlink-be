package com.greedy.meetlink.result.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.result.entity.MeetingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MeetingResultRepository extends JpaRepository<MeetingResult, Long> {
    Optional<MeetingResult> findByMeeting(Meeting meeting);
}
