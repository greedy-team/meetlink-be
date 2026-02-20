package com.greedy.meetlink.participant.entity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.greedy.meetlink.meeting.entity.Meeting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ParticipantTest {
    @Test
    @DisplayName("참여자를 생성하면 닉네임과 토큰이 정확하게 설정된다.")
    void create_participant_success() {
        // given
        Meeting meeting = Meeting.builder().name("그리디").code("TESTCODE").build();

        String nickname = "테스트유저";
        String token = "TOKEN-123";

        // when
        Participant participant = Participant.create(meeting, nickname, token);

        // then
        assertThat(participant.getNickname()).isEqualTo(nickname);
        assertThat(participant.getMeeting()).isEqualTo(meeting);
        assertThat(participant.getToken()).isEqualTo(token);
    }
}
