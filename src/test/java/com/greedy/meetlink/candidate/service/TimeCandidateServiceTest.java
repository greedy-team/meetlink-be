package com.greedy.meetlink.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.config.MockClientConfig;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.entity.Participant;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Import(MockClientConfig.class)
class TimeCandidateServiceTest {
    @Autowired private TimeCandidateService timeCandidateService;
    @Autowired private TimeAvailabilityRepository timeAvailabilityRepository;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private MeetingRepository meetingRepository;
    @Autowired private MeetingResultRepository meetingResultRepository;

    @Test
    @DisplayName("후보 시간과 히트맵이 DB 기반으로 정상 추출되어야 한다.")
    void calculateAndSave_Success() {
        // given
        String meetingCode = "TEST-123";
        LocalDate testDate = LocalDate.of(2026, 2, 24);

        Meeting meeting =
                meetingRepository.save(
                        Meeting.builder()
                                .name("테스트 모임")
                                .code(meetingCode)
                                .enableTimeRecommendation(true)
                                .timeRangeStart(LocalTime.of(9, 0))
                                .timeRangeEnd(LocalTime.of(12, 0))
                                .build());
        meetingResultRepository.save(MeetingResult.create(meeting));

        Participant p1 =
                participantRepository.save(
                        Participant.builder()
                                .meeting(meeting)
                                .nickname("유저1")
                                .token("token1")
                                .timeSubmittedAt(LocalDateTime.now())
                                .build());
        Participant p2 =
                participantRepository.save(
                        Participant.builder()
                                .meeting(meeting)
                                .nickname("유저2")
                                .token("token2")
                                .timeSubmittedAt(LocalDateTime.now())
                                .build());

        timeAvailabilityRepository.saveAll(
                List.of(
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(p1)
                                .date(testDate)
                                .startTime(LocalTime.of(10, 0))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(p1)
                                .date(testDate)
                                .startTime(LocalTime.of(10, 30))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(p2)
                                .date(testDate)
                                .startTime(LocalTime.of(10, 0))
                                .build()));

        // when
        List<TimeCandidateResponse> response =
                timeCandidateService.calculateTimeCandidates(meetingCode);

        // then
        assertThat(response).hasSize(2);

        // 첫 번째 후보
        TimeCandidateResponse first = response.getFirst();
        assertThat(first.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(first.getEndTime()).isEqualTo(LocalTime.of(10, 30)); // 묶인 상태
        assertThat(first.getAvailableCount()).isEqualTo(2);

        // 두 번째 후보
        TimeCandidateResponse second = response.get(1);
        assertThat(second.getStartTime()).isEqualTo(LocalTime.of(10, 30));
        assertThat(second.getEndTime()).isEqualTo(LocalTime.of(11, 0)); // 묶인 상태
        assertThat(second.getAvailableCount()).isEqualTo(1);
    }
}
