package com.greedy.meetlink.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.dto.response.MeetingResponse;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock private MeetingRepository meetingRepository;

    @InjectMocks private MeetingService meetingService;

    @Test
    @DisplayName("모임 코드로 모임 기본 정보를 정확히 조회한다.")
    void getMeetingDetail_Success() {
        // given
        String code = "TEST_CODE";

        Meeting mockMeeting = mock(Meeting.class);
        given(mockMeeting.getId()).willReturn(1L);
        given(mockMeeting.getName()).willReturn("그리디 모임");
        given(mockMeeting.getCode()).willReturn(code);
        given(mockMeeting.getTimeRangeStart()).willReturn(LocalTime.of(10, 0));
        given(mockMeeting.getTimeRangeEnd()).willReturn(LocalTime.of(22, 0));

        given(mockMeeting.isEnableTimeRecommendation()).willReturn(true);
        given(mockMeeting.isEnablePlaceRecommendation()).willReturn(true);

        given(meetingRepository.findByCode(code)).willReturn(Optional.of(mockMeeting));

        // when
        MeetingResponse response = meetingService.getMeetingDetail(code);

        // then
        // 기본 정보 검증
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("그리디 모임");
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getTimeRangeStart()).isEqualTo(LocalTime.of(10, 0));
        assertThat(response.getTimeRangeEnd()).isEqualTo(LocalTime.of(22, 0));
    }

    @Test
    @DisplayName("존재하지 않는 코드로 조회 시 예외가 발생한다.")
    void getMeetingDetail_Fail_NotFound() {
        // given
        String wrongCode = "WRONG";
        given(meetingRepository.findByCode(wrongCode)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> meetingService.getMeetingDetail(wrongCode))
                .isInstanceOf(MeetingNotFoundException.class);
    }
}
