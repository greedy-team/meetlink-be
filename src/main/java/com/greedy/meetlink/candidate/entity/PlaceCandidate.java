package com.greedy.meetlink.candidate.entity;

import com.greedy.meetlink.candidate.PlaceCalculationType;
import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.entity.Meeting;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceCalculationType calculationType;
}
