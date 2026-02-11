package com.greedy.meetlink.meeting.repository;

import com.greedy.meetlink.meeting.entity.Meeting;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findByCode(String code);

    boolean existsByCode(String code);
}
