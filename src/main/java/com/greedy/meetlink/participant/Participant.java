package com.greedy.meetlink.participant;

import com.greedy.meetlink.availability.TimeAvailability;
import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.entity.Meeting;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private StartPoint startPoint;

    @OneToMany(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TimeAvailability> timeAvailabilities = new ArrayList<>();

    public Participant(Meeting meeting, String nickname, String token) {
        this.meeting = meeting;
        this.nickname = nickname;
        this.token = token;
    }
}
