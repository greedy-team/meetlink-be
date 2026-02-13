package com.greedy.meetlink.availability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimeAvailabilityRepository extends JpaRepository<TimeAvailability, Long> {
    /**
     * 모임 ID로 모든 TimeAvailability 조회
     */
    List<TimeAvailability> findByMeetingId(Long meetingId);

    /**
     * 모임 코드로 모든 TimeAvailability 조회 (히트맵 생성용)
     */
    @Query("SELECT ta FROM TimeAvailability ta " +
            "JOIN FETCH ta.meeting m " +
            "WHERE m.code = :code")
    List<TimeAvailability> findByMeetingCode(@Param("code") String code);
}
