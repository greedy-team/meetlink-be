package com.greedy.meetlink.candidate.entity;

import com.greedy.meetlink.common.client.dto.RouteInfo;
import com.greedy.meetlink.common.client.dto.SegmentInfo;
import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.participant.entity.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;
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
            @Index(name = "idx_place_candidate_route_candidate", columnList = "place_candidate_id")
        })
public class PlaceCandidateRoute extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "place_candidate_id", nullable = false)
    private PlaceCandidate placeCandidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(nullable = false)
    private double travelTime;

    @Convert(converter = SegmentInfoListConverter.class)
    @Column(columnDefinition = "TEXT", nullable = false)
    private List<SegmentInfo> segments;

    @Builder
    private PlaceCandidateRoute(
            PlaceCandidate placeCandidate,
            Participant participant,
            double travelTime,
            List<SegmentInfo> segments) {
        this.placeCandidate = placeCandidate;
        this.participant = participant;
        this.travelTime = travelTime;
        this.segments = segments;
    }

    public static PlaceCandidateRoute create(
            PlaceCandidate placeCandidate, Participant participant, RouteInfo route) {
        return PlaceCandidateRoute.builder()
                .placeCandidate(placeCandidate)
                .participant(participant)
                .travelTime(route.travelTime())
                .segments(route.segments())
                .build();
    }
}
