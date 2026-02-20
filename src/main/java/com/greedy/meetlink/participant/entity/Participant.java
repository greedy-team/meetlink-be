package com.greedy.meetlink.participant.entity;

import com.greedy.meetlink.availability.TimeAvailability;
import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.entity.Meeting;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"meeting_id", "token"})})
public class Participant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String token;

    @Builder.Default
    @OneToMany(
            mappedBy = "participant",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<TimeAvailability> availableTimes = new ArrayList<>();

    @OneToOne(
            mappedBy = "participant",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private StartPoint startPoint;

    public static Participant create(Meeting meeting, String nickname, String token) {
        return Participant.builder().meeting(meeting).nickname(nickname).token(token).build();
    }

    public boolean hasEnteredTime() {
        if (!this.meeting.isEnableTimeRecommendation()) {
            return true;
        }

        return !this.availableTimes.isEmpty();
    }

    public boolean hasEnteredPlace() {
        if (!this.meeting.isEnablePlaceRecommendation()) {
            return true;
        }
        return this.startPoint != null;
    }
}
