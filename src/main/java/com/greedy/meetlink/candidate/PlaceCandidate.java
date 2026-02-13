package com.greedy.meetlink.candidate;

import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.result.entity.PlaceTravelInfo;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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

    @OneToMany(mappedBy = "placeCandidate", cascade = CascadeType.ALL)
    private List<PlaceTravelInfo> travelInfos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaceCalculationType calculationType;

    @Column(nullable = false)
    private int rank;
}
