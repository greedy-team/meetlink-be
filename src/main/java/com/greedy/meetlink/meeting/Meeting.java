package com.greedy.meetlink.meeting;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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

    private Meeting(String name, String code, LocalTime start, LocalTime end) {
        this.name = name;
        this.code = code;
        this.timeRangeStart = start;
        this.timeRangeEnd = end;
    }

    public static Meeting create(String name, String code, LocalTime start, LocalTime end) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        return new Meeting(name, code, start, end);
    }

    public boolean isRecommendationCompleted() {
        return this.meetingResult != null;
    }
}
