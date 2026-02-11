package com.greedy.meetlink.meeting.entity;

import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.participant.Participant;
import com.greedy.meetlink.result.MeetingResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    private boolean enableTimeRecommendation;
    private boolean enablePlaceRecommendation;

    @Enumerated(EnumType.STRING)
    private TimeAvailabilityType timeAvailabilityType;

    private LocalTime timeRangeStart;
    private LocalTime timeRangeEnd;

    @OneToMany(mappedBy = "meeting")
    private List<Participant> participants = new ArrayList<>();

    @OneToOne(mappedBy = "meeting", fetch = FetchType.LAZY)
    private MeetingResult meetingResult;

    @Builder
    public Meeting(
            String name,
            String code,
            boolean enableTimeRecommendation,
            boolean enablePlaceRecommendation,
            TimeAvailabilityType timeAvailabilityType,
            LocalTime timeRangeStart,
            LocalTime timeRangeEnd) {
        this.name = name;
        this.code = code;
        this.enableTimeRecommendation = enableTimeRecommendation;
        this.enablePlaceRecommendation = enablePlaceRecommendation;
        this.timeAvailabilityType = timeAvailabilityType;
        this.timeRangeStart = timeRangeStart;
        this.timeRangeEnd = timeRangeEnd;
    }

    public void update(
            String name,
            boolean enableTimeRecommendation,
            boolean enablePlaceRecommendation,
            TimeAvailabilityType timeAvailabilityType,
            LocalTime timeRangeStart,
            LocalTime timeRangeEnd) {
        this.name = name;
        this.enableTimeRecommendation = enableTimeRecommendation;
        this.enablePlaceRecommendation = enablePlaceRecommendation;
        this.timeAvailabilityType = timeAvailabilityType;
        this.timeRangeStart = timeRangeStart;
        this.timeRangeEnd = timeRangeEnd;
    }

    public boolean isRecommendationCompleted() {
        return this.meetingResult != null;
    }
}
