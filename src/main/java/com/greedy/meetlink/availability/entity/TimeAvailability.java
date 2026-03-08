package com.greedy.meetlink.availability.entity;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
            @Index(
                    name = "idx_time_meeting_participant",
                    columnList = "meeting_id, participant_id"),
            @Index(name = "idx_time_meeting", columnList = "meeting_id"),
            @Index(name = "idx_time_meeting_start_time", columnList = "meeting_id, start_time")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    columnNames = {
                        "meeting_id",
                        "participant_id",
                        "date",
                        "day_of_week",
                        "start_time"
                    })
        })
public class TimeAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    private LocalDate date;
    private Integer dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Builder
    private TimeAvailability(
            Meeting meeting,
            Participant participant,
            LocalDate date,
            Integer dayOfWeek,
            LocalTime startTime) {
        this.meeting = meeting;
        this.participant = participant;
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
    }

    public static TimeAvailability create(
            Meeting meeting,
            Participant participant,
            LocalDate date,
            Integer dayOfWeek,
            LocalTime startTime) {
        return TimeAvailability.builder()
                .meeting(meeting)
                .participant(participant)
                .date(date)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .build();
    }
}
