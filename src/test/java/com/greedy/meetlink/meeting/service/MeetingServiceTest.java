    package com.greedy.meetlink.meeting.service;

    import com.greedy.meetlink.meeting.dto.response.MeetingDetailResponse;
    import com.greedy.meetlink.meeting.entity.Meeting;
    import com.greedy.meetlink.meeting.repository.MeetingRepository;
    import com.greedy.meetlink.participant.Participant;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.server.ResponseStatusException;

    import java.time.LocalTime;
    import java.util.List;
    import java.util.Optional;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.assertj.core.api.Assertions.assertThatThrownBy;
    import static org.mockito.BDDMockito.given;
    import static org.mockito.Mockito.mock;

    @ExtendWith(MockitoExtension.class)
    class MeetingServiceTest {

        @Mock
        private MeetingRepository meetingRepository;

        @InjectMocks
        private MeetingService meetingService;

        @Test
        @DisplayName("참여자 정보, 출발지 여부, 추천 결과가 모두 정확히 매핑된다.")
        void getMeetingDetail_FullCheck() {
            // given
            String code = "FULL_CHECK_CODE";

            // 1. 참여자 1: 장소 O, 시간 X
            Participant userHasPlace = mock(Participant.class);
            given(userHasPlace.getId()).willReturn(10L);
            given(userHasPlace.getNickname()).willReturn("장소만_입력한_유저");
            given(userHasPlace.hasEnteredPlace()).willReturn(true);
            given(userHasPlace.hasEnteredTime()).willReturn(false);

            // 2. 참여자 2: 장소 X, 시간 O
            Participant userHasTime = mock(Participant.class);
            given(userHasTime.getId()).willReturn(20L);
            given(userHasTime.getNickname()).willReturn("시간만_입력한_유저");
            given(userHasTime.hasEnteredPlace()).willReturn(false);
            given(userHasTime.hasEnteredTime()).willReturn(true);

            Meeting mockMeeting = mock(Meeting.class);
            given(mockMeeting.getId()).willReturn(1L);
            given(mockMeeting.getName()).willReturn("그리디 모임");
            given(mockMeeting.getCode()).willReturn(code);
            given(mockMeeting.getTimeRangeStart()).willReturn(LocalTime.of(10, 0));
            given(mockMeeting.getTimeRangeEnd()).willReturn(LocalTime.of(22, 0));

            given(mockMeeting.isEnableTimeRecommendation()).willReturn(true);
            given(mockMeeting.isEnablePlaceRecommendation()).willReturn(true);

            given(mockMeeting.getParticipants()).willReturn(List.of(userHasPlace, userHasTime));
            given(mockMeeting.isRecommendationCompleted()).willReturn(true); // 추천 완료 여부 모킹

            given(meetingRepository.findByCode(code)).willReturn(Optional.of(mockMeeting));

            // when
            MeetingDetailResponse response = meetingService.getMeetingDetail(code);

            // then
            // 기본 정보 검증
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getName()).isEqualTo("그리디 모임");
            assertThat(response.getCode()).isEqualTo(code);
            assertThat(response.getTimeRangeStart()).isEqualTo(LocalTime.of(10, 0));
            assertThat(response.getTimeRangeEnd()).isEqualTo(LocalTime.of(22, 0));
            assertThat(response.getParticipants()).hasSize(2);

            // 추천 완료 여부 검증
            assertThat(response.isRecommendationCompleted()).isTrue();

            // 참여자 목록 검증
            assertThat(response.getParticipants()).hasSize(2);

            // 첫 번째 유저 (장소 O, 시간 X)
            assertThat(response.getParticipants())
                    .filteredOn(p -> p.getNickname().equals("장소만_입력한_유저"))
                    .satisfiesExactly(p -> {
                        assertThat(p.isHasEnteredPlace()).isTrue();
                        assertThat(p.isHasEnteredTime()).isFalse();
                    });

            // 두 번째 유저 (장소 X, 시간 O)
            assertThat(response.getParticipants())
                    .filteredOn(p -> p.getNickname().equals("시간만_입력한_유저"))
                    .satisfiesExactly(p -> {
                        assertThat(p.isHasEnteredPlace()).isFalse();
                        assertThat(p.isHasEnteredTime()).isTrue();
                    });
        }


        @Test
        @DisplayName("존재하지 않는 코드로 조회 시 예외가 발생한다.")
        void getMeetingDetail_Fail_NotFound() {
            // given
            String wrongCode = "WRONG";
            given(meetingRepository.findByCode(wrongCode)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> meetingService.getMeetingDetail(wrongCode))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
