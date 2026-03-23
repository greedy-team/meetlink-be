package com.greedy.meetlink.meeting.repository;

import com.greedy.meetlink.availability.entity.TimeAvailabilityType;
import com.greedy.meetlink.meeting.entity.Meeting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    Optional<Meeting> findByCode(String code);

    boolean existsByCode(String code);

    @Query(
            """
        SELECT COUNT(m) FROM Meeting m
        WHERE (SELECT COUNT(p) FROM Participant p WHERE p.meeting = m) <= 1
    """)
    long countOrphanMeetings();

    long countByEnableTimeRecommendationTrue();

    long countByEnablePlaceRecommendationTrue();

    long countByTimeAvailabilityType(TimeAvailabilityType type);
}
