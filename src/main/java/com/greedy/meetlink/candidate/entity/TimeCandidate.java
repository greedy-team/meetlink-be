package com.greedy.meetlink.candidate.entity;

import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.entity.Meeting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeCandidate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Meeting meeting;

    private LocalDate date;
    private Integer dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private int availableCount;

    @Column(nullable = false)
    private int rank;

    @Builder
    private TimeCandidate(
            Meeting meeting,
            LocalDate date,
            Integer dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            int availableCount) {
        this.meeting = meeting;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.availableCount = availableCount;
        this.rank = 0;
    }

    public static TimeCandidate create(
            Meeting meeting,
            LocalDate date,
            Integer dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            int availableCount) {
        return TimeCandidate.builder()
                .meeting(meeting)
                .date(date)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .availableCount(availableCount)
                .build();
    }

    public void assignRank(int rank) {
        this.rank = rank;
    }
}
