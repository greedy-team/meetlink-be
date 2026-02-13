package com.greedy.meetlink.result.entity;

import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.candidate.TimeCandidate;
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
    @JoinColumn(nullable = false)
    private Meeting meeting;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private TimeCandidate timeCandidate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private PlaceCandidate placeCandidate;
}
