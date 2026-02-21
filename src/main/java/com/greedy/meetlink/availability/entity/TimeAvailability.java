package com.greedy.meetlink.availability.entity;

import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints =
                @UniqueConstraint(
                        columnNames = {
                            "meeting_id",
                            "participant_id",
                            "date",
                            "day_of_week",
                            "start_time"
                        }))
public class TimeAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Meeting meeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Participant participant;

    private LocalDate date;
    private Integer dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;
}
