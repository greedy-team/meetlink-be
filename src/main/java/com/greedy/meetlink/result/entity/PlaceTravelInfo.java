package com.greedy.meetlink.result.entity;

import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.participant.Participant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

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

    private double travelTime; // 참여자 소요 시간

    @Column(columnDefinition = "TEXT")
    private String routeData; // 이동 경로 데이터 (JSON String 등)
}
