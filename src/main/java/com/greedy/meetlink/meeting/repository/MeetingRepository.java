package com.greedy.meetlink.meeting.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    boolean existsByCode(String code);
}
