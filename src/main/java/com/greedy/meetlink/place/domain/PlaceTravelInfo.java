package com.greedy.meetlink.place.domain;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.participant.entity.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceTravelInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_candidate_id")
    private PlaceCandidate placeCandidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id")
    private Participant participant;

    private double travelTime;

    @Column(columnDefinition = "TEXT")
    private String routeData;

    @Builder
    public PlaceTravelInfo(
            PlaceCandidate placeCandidate,
            Participant participant,
            double travelTime,
            String routeData) {
        this.placeCandidate = placeCandidate;
        this.participant = participant;
        this.travelTime = travelTime;
        this.routeData = routeData;
    }
}
