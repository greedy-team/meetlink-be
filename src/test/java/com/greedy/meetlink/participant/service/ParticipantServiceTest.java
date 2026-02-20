package com.greedy.meetlink.participant.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.greedy.meetlink.common.exception.DuplicateNicknameException;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.dto.request.ParticipantJoinRequest;
import com.greedy.meetlink.participant.dto.response.ParticipantJoinResponse;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ParticipantServiceTest {
    @Autowired ParticipantService participantService;
    @Autowired ParticipantRepository participantRepository;
    @Autowired MeetingRepository meetingRepository;

    @Test
    @DisplayName("존재하는 모임에 정상적으로 참여하고 토큰을 발급받는다.")
    void join_meeting_success() {
        // given
        Meeting meeting =
                meetingRepository.save(Meeting.builder().name("그리디").code("MEET123").build());

        ParticipantJoinRequest request = new ParticipantJoinRequest("테스트유저");

        // when
        ParticipantJoinResponse response = participantService.join("MEET123", request);

        // then
        assertThat(response.getToken()).isNotNull();

        Participant savedParticipant =
                participantRepository
                        .findByMeeting_CodeAndToken("MEET123", response.getToken())
                        .orElseThrow();
        assertThat(savedParticipant.getNickname()).isEqualTo("테스트유저");
    }

    @Test
    @DisplayName("존재하지 않는 모임 코드라면 예외가 발생한다.")
    void join_meeting_fail_invalid_code() {
        // given
        String invalidCode = "INVALID";
        ParticipantJoinRequest request = new ParticipantJoinRequest("테스트유저");

        // when & then
        assertThatThrownBy(() -> participantService.join(invalidCode, request))
                .isInstanceOf(MeetingNotFoundException.class)
                .hasMessage("모임을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("이미 존재하는 닉네임으로 참여하면 예외가 발생한다.")
    void join_meeting_fail_duplicate_nickname() {
        // given
        Meeting meeting =
                meetingRepository.save(Meeting.builder().name("그리디").code("MEET123").build());

        participantService.join("MEET123", new ParticipantJoinRequest("테스트유저"));

        ParticipantJoinRequest duplicateRequest = new ParticipantJoinRequest("테스트유저");

        // when & then
        assertThatThrownBy(() -> participantService.join("MEET123", duplicateRequest))
                .isInstanceOf(DuplicateNicknameException.class)
                .hasMessage("이미 사용 중인 닉네임입니다.");
    }
}
