package com.greedy.meetlink.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateListResponse;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.candidate.repository.TimeCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimeCandidateServiceTest {

    @Mock private TimeCandidateRepository timeCandidateRepository;

    @Mock private TimeAvailabilityRepository timeAvailabilityRepository;

    @InjectMocks private TimeCandidateService timeCandidateService;

    @Test
    @DisplayName("인원이 가장 많은 시간이 1위로 추출되어야 한다.")
    void calculateAndSave_Success() {
        // given
        String meetingCode = "TEST-123";
        LocalDate testDate = LocalDate.of(2026, 2, 24);

        Meeting meeting = Meeting.builder().code(meetingCode).build();
        Participant p1 = Participant.builder().meeting(meeting).nickname("유저1").build();
        Participant p2 = Participant.builder().meeting(meeting).nickname("유저2").build();

        TimeAvailability t1 =
                TimeAvailability.builder()
                        .participant(p1)
                        .date(testDate)
                        .startTime(LocalTime.of(10, 0))
                        .build();
        TimeAvailability t2 =
                TimeAvailability.builder()
                        .participant(p1)
                        .date(testDate)
                        .startTime(LocalTime.of(10, 30))
                        .build();
        TimeAvailability t3 =
                TimeAvailability.builder()
                        .participant(p2)
                        .date(testDate)
                        .startTime(LocalTime.of(10, 0))
                        .build();
        List<TimeAvailability> mockData = List.of(t1, t2, t3);

        when(timeAvailabilityRepository.findLatestModifiedAtByMeetingCode(meetingCode))
                .thenReturn(Optional.of(LocalDateTime.now()));
        when(timeCandidateRepository.findLatestCreatedAtByMeetingCode(meetingCode))
                .thenReturn(Optional.empty());

        when(timeAvailabilityRepository.findByMeetingCode(meetingCode)).thenReturn(mockData);

        // when
        TimeCandidateListResponse response = timeCandidateService.calculateAndSave(meetingCode);

        // then
        verify(timeCandidateRepository, times(1)).deleteByMeetingCode(meetingCode);
        verify(timeCandidateRepository, times(1)).saveAll(any());

        assertThat(response).isNotNull();
        assertThat(response.heatmaps()).hasSize(1);
        assertThat(response.heatmaps().get(0).slots()).hasSize(2);

        List<TimeCandidateResponse> rankings = response.rankings();
        assertThat(rankings).hasSize(2);

        assertThat(rankings.get(0).getRank()).isEqualTo(1);
        assertThat(rankings.get(0).getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(rankings.get(0).getAvailableCount()).isEqualTo(2);

        assertThat(rankings.get(1).getRank()).isEqualTo(2);
        assertThat(rankings.get(1).getStartTime()).isEqualTo(LocalTime.of(10, 30));
        assertThat(rankings.get(1).getAvailableCount()).isEqualTo(1);
    }
}
