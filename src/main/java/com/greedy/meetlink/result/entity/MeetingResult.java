package com.greedy.meetlink.result.entity;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.entity.TimeCandidate;
import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.entity.Meeting;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingResult extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_candidate_id")
    private TimeCandidate timeCandidate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_candidate_id")
    private PlaceCandidate placeCandidate;

    @Builder
    private MeetingResult(Meeting meeting) {
        this.meeting = meeting;
    }

    public static MeetingResult create(Meeting meeting) {
        return MeetingResult.builder().meeting(meeting).build();
    }

    public void updatePlaceCandidate(PlaceCandidate placeCandidate) {
        this.placeCandidate = placeCandidate;
    }
}
