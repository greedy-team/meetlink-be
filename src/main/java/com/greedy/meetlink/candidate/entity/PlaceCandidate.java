package com.greedy.meetlink.candidate.entity;

import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.entity.Meeting;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        indexes = {
            @Index(name = "idx_place_candidate_meeting_rank", columnList = "meeting_id, rank")
        })
public class PlaceCandidate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private double avgTravelTime;

    @Column(nullable = false)
    private double maxTravelTime;

    @Column(nullable = false)
    private int rank;

    @Builder
    private PlaceCandidate(
            Meeting meeting,
            String name,
            String address,
            double latitude,
            double longitude,
            double avgTravelTime,
            double maxTravelTime) {
        this.meeting = meeting;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.avgTravelTime = avgTravelTime;
        this.maxTravelTime = maxTravelTime;
        this.rank = 0;
    }

    public static PlaceCandidate create(
            Meeting meeting,
            String name,
            String address,
            double latitude,
            double longitude,
            double avgTravelTime,
            double maxTravelTime) {
        return PlaceCandidate.builder()
                .meeting(meeting)
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .avgTravelTime(avgTravelTime)
                .maxTravelTime(maxTravelTime)
                .build();
    }

    public void assignRank(int rank) {
        this.rank = rank;
    }
}
