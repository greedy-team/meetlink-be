package com.greedy.meetlink.result;

import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.candidate.TimeCandidate;
import com.greedy.meetlink.common.entity.BaseEntity;
import com.greedy.meetlink.meeting.Meeting;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MeetingResult extends BaseEntity {
    @Id private Long meetingId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id")
    private Meeting meeting;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_candidate_id")
    private TimeCandidate timeCandidate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_candidate_id")
    private PlaceCandidate placeCandidate;
}
