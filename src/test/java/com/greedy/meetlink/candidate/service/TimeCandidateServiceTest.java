package com.greedy.meetlink.candidate.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.availability.entity.TimeAvailabilityType;
import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.candidate.entity.TimeCandidate;
import com.greedy.meetlink.candidate.repository.TimeCandidateRepository;
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
    @Autowired private TimeCandidateRepository timeCandidateRepository;
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
        timeCandidateService.calculateTimeCandidates(meetingCode);

        // then
        List<TimeCandidate> saved = timeCandidateRepository.findByMeetingOrderByRankAsc(meeting);
        assertThat(saved).hasSize(2);

        // 첫 번째 후보
        TimeCandidate first = saved.getFirst();
        assertThat(first.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(first.getEndTime()).isEqualTo(LocalTime.of(10, 30)); // 묶인 상태
        assertThat(first.getAvailableCount()).isEqualTo(2);

        // 두 번째 후보
        TimeCandidate second = saved.get(1);
        assertThat(second.getStartTime()).isEqualTo(LocalTime.of(10, 30));
        assertThat(second.getEndTime()).isEqualTo(LocalTime.of(11, 0)); // 묶인 상태
        assertThat(second.getAvailableCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("날짜가 다르면 시간이 이어져도 병합되지 않고 각각 분리되어야 한다.")
    void calculate_NotMergeDifferentDays_Success() {
        // given
        String meetingCode = "TEST-456";
        LocalDate date1 = LocalDate.of(2026, 2, 23);
        LocalDate date2 = LocalDate.of(2026, 2, 24);

        Meeting meeting =
                meetingRepository.save(
                        Meeting.builder()
                                .name("테스트 모임2")
                                .code(meetingCode)
                                .enableTimeRecommendation(true)
                                .timeRangeStart(LocalTime.of(9, 0))
                                .timeRangeEnd(LocalTime.of(23, 0))
                                .build());
        meetingResultRepository.save(MeetingResult.create(meeting));

        Participant p1 =
                participantRepository.save(
                        Participant.builder()
                                .meeting(meeting)
                                .nickname("유저1")
                                .token("token3")
                                .timeSubmittedAt(LocalDateTime.now())
                                .build());

        timeAvailabilityRepository.saveAll(
                List.of(
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(p1)
                                .date(date1)
                                .startTime(LocalTime.of(15, 0))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(p1)
                                .date(date2)
                                .startTime(LocalTime.of(15, 30))
                                .build()));

        // when
        timeCandidateService.calculateTimeCandidates(meetingCode);

        // then
        List<TimeCandidate> saved = timeCandidateRepository.findByMeetingOrderByRankAsc(meeting);
        assertThat(saved).hasSize(2);

        // 첫 번째 후보
        TimeCandidate first = saved.getFirst();
        assertThat(first.getDate()).isEqualTo(date1);
        assertThat(first.getStartTime()).isEqualTo(LocalTime.of(15, 0));
        assertThat(first.getEndTime()).isEqualTo(LocalTime.of(15, 30));

        // 두 번째 후보
        TimeCandidate second = saved.get(1);
        assertThat(second.getDate()).isEqualTo(date2);
        assertThat(second.getStartTime()).isEqualTo(LocalTime.of(15, 30));
        assertThat(second.getEndTime()).isEqualTo(LocalTime.of(16, 0));
    }

    @Test
    @DisplayName("인원수가 같아도 참가자 구성이 다르면 병합되지 않아야 한다.")
    void calculate_NotMergeDifferentParticipants_Success() {
        // given
        String meetingCode = "TEST-789";
        LocalDate testDate = LocalDate.of(2026, 2, 25);

        Meeting meeting =
                meetingRepository.save(
                        Meeting.builder()
                                .name("테스트 모임3")
                                .code(meetingCode)
                                .enableTimeRecommendation(true)
                                .timeRangeStart(LocalTime.of(9, 0))
                                .timeRangeEnd(LocalTime.of(23, 0))
                                .build());
        meetingResultRepository.save(MeetingResult.create(meeting));

        Participant user1 =
                participantRepository.save(
                        Participant.builder()
                                .meeting(meeting)
                                .nickname("유저1")
                                .token("token4")
                                .timeSubmittedAt(LocalDateTime.now())
                                .build());

        Participant user2 =
                participantRepository.save(
                        Participant.builder()
                                .meeting(meeting)
                                .nickname("유저2")
                                .token("token5")
                                .timeSubmittedAt(LocalDateTime.now())
                                .build());

        timeAvailabilityRepository.saveAll(
                List.of(
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(user1)
                                .date(testDate)
                                .startTime(LocalTime.of(16, 0))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(user1)
                                .date(testDate)
                                .startTime(LocalTime.of(16, 30))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(user2)
                                .date(testDate)
                                .startTime(LocalTime.of(17, 0))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(user2)
                                .date(testDate)
                                .startTime(LocalTime.of(17, 30))
                                .build()));

        // when
        timeCandidateService.calculateTimeCandidates(meetingCode);

        // then
        List<TimeCandidate> saved = timeCandidateRepository.findByMeetingOrderByRankAsc(meeting);
        assertThat(saved).hasSize(2);

        // 첫 번째 후보
        TimeCandidate first = saved.get(0);
        assertThat(first.getStartTime()).isEqualTo(LocalTime.of(16, 0));
        assertThat(first.getEndTime()).isEqualTo(LocalTime.of(17, 0));
        assertThat(first.getAvailableCount()).isEqualTo(1);

        // 두 번째 후보
        TimeCandidate second = saved.get(1);
        assertThat(second.getStartTime()).isEqualTo(LocalTime.of(17, 0));
        assertThat(second.getEndTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(second.getAvailableCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("SPECIFIC_DATE 타입에서 과거 시간은 조회 결과에서 제외되어야 한다.")
    void getTimeCandidates_FilterPastTimes_Success() {
        // given
        String meetingCode = "PAST-TEST";
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        Meeting meeting =
                meetingRepository.save(
                        Meeting.builder()
                                .name("과거 시간 필터링 테스트")
                                .code(meetingCode)
                                .timeAvailabilityType(TimeAvailabilityType.SPECIFIC_DATE)
                                .enableTimeRecommendation(true)
                                .timeRangeStart(LocalTime.of(9, 0))
                                .timeRangeEnd(LocalTime.of(23, 0))
                                .build());
        meetingResultRepository.save(MeetingResult.create(meeting));

        Participant participant =
                participantRepository.save(
                        Participant.builder()
                                .meeting(meeting)
                                .nickname("테스트유저")
                                .token("past-token")
                                .timeSubmittedAt(LocalDateTime.now())
                                .build());

        // 과거 시간과 미래 시간을 DB에 저장
        timeAvailabilityRepository.saveAll(
                List.of(
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(participant)
                                .date(yesterday)
                                .startTime(LocalTime.of(10, 0))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(participant)
                                .date(tomorrow)
                                .startTime(LocalTime.of(10, 0))
                                .build()));

        // 후보 계산
        timeCandidateService.calculateTimeCandidates(meetingCode);

        // when - 과거/미래 시간이 모두 저장되었는지 확인
        List<TimeCandidate> allSaved = timeCandidateRepository.findByMeetingOrderByRankAsc(meeting);
        assertThat(allSaved).hasSize(2);

        // 실제 조회 결과에서는 과거 시간이 필터링되어야 함
        List<TimeCandidateResponse> responses =
                timeCandidateService.getTimeCandidates(meetingCode, "past-token");

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().getDate()).isEqualTo(tomorrow);
    }

    @Test
    @DisplayName("SPECIFIC_DATE 타입에서 미래 시간만 있으면 모두 조회되어야 한다.")
    void getTimeCandidates_FutureTimes_AllReturned() {
        // given
        String meetingCode = "FUTURE-TEST";
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfterTomorrow = LocalDate.now().plusDays(2);

        Meeting meeting =
                meetingRepository.save(
                        Meeting.builder()
                                .name("미래 시간만 테스트")
                                .code(meetingCode)
                                .timeAvailabilityType(TimeAvailabilityType.SPECIFIC_DATE)
                                .enableTimeRecommendation(true)
                                .timeRangeStart(LocalTime.of(9, 0))
                                .timeRangeEnd(LocalTime.of(23, 0))
                                .build());
        meetingResultRepository.save(MeetingResult.create(meeting));

        Participant participant =
                participantRepository.save(
                        Participant.builder()
                                .meeting(meeting)
                                .nickname("테스트유저2")
                                .token("future-token")
                                .timeSubmittedAt(LocalDateTime.now())
                                .build());

        // 미래 시간만 저장
        timeAvailabilityRepository.saveAll(
                List.of(
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(participant)
                                .date(tomorrow)
                                .startTime(LocalTime.of(10, 0))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(participant)
                                .date(dayAfterTomorrow)
                                .startTime(LocalTime.of(14, 0))
                                .build()));

        timeCandidateService.calculateTimeCandidates(meetingCode);

        // when
        List<TimeCandidateResponse> responses =
                timeCandidateService.getTimeCandidates(meetingCode, "future-token");

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.stream().map(TimeCandidateResponse::getDate))
                .containsExactlyInAnyOrder(tomorrow, dayAfterTomorrow);
    }

    @Test
    @DisplayName("WEEKLY 타입에서는 필터링이 적용되지 않아야 한다.")
    void getTimeCandidates_WeeklyType_NoFiltering() {
        // given
        String meetingCode = "WEEKLY-TEST";

        Meeting meeting =
                meetingRepository.save(
                        Meeting.builder()
                                .name("주간 반복 테스트")
                                .code(meetingCode)
                                .timeAvailabilityType(TimeAvailabilityType.WEEKLY)
                                .enableTimeRecommendation(true)
                                .timeRangeStart(LocalTime.of(9, 0))
                                .timeRangeEnd(LocalTime.of(23, 0))
                                .build());
        meetingResultRepository.save(MeetingResult.create(meeting));

        Participant participant =
                participantRepository.save(
                        Participant.builder()
                                .meeting(meeting)
                                .nickname("테스트유저3")
                                .token("weekly-token")
                                .timeSubmittedAt(LocalDateTime.now())
                                .build());

        // WEEKLY 타입은 dayOfWeek 사용 (월=1 ~ 일=7)
        timeAvailabilityRepository.saveAll(
                List.of(
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(participant)
                                .dayOfWeek(1) // 월요일
                                .startTime(LocalTime.of(10, 0))
                                .build(),
                        TimeAvailability.builder()
                                .meeting(meeting)
                                .participant(participant)
                                .dayOfWeek(3) // 수요일
                                .startTime(LocalTime.of(14, 0))
                                .build()));

        timeCandidateService.calculateTimeCandidates(meetingCode);

        // when
        List<TimeCandidateResponse> responses =
                timeCandidateService.getTimeCandidates(meetingCode, "weekly-token");

        // then - WEEKLY는 반복이므로 필터링 없이 모두 반환
        assertThat(responses).hasSize(2);
        assertThat(responses.stream().map(TimeCandidateResponse::getDayOfWeek))
                .containsExactlyInAnyOrder(1, 3);
    }
}
