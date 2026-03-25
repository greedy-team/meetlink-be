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
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE time_availability SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(
        name = "time_availability",
        indexes = {
            @Index(
                    name = "idx_time_meeting_participant",
                    columnList = "meeting_id, participant_id"),
            @Index(name = "idx_time_meeting", columnList = "meeting_id"),
            @Index(name = "idx_time_meeting_start_time", columnList = "meeting_id, start_time")
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

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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
