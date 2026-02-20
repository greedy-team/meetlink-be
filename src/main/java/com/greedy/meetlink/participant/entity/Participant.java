package com.greedy.meetlink.participant.entity;

import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.entity.Meeting;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"meeting_id", "token"}),
                @UniqueConstraint(columnNames = {"meeting_id", "nickname"})
        }
)
public class Participant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String token;

    @Builder
    private Participant(Meeting meeting, String nickname, String token) {
        this.meeting = meeting;
        this.nickname = nickname;
        this.token = token;
    }

    public static Participant create(Meeting meeting, String nickname, String token) {
        return Participant.builder()
                .meeting(meeting)
                .nickname(nickname)
                .token(token)
                .build();
    }
}
