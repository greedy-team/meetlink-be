package com.greedy.meetlink.availability.service;

import com.greedy.meetlink.availability.dto.request.TimeAvailabilityRequest;
import com.greedy.meetlink.availability.dto.response.TimeAvailabilitiesResponse;
import com.greedy.meetlink.availability.dto.response.TimeAvailabilityResponse;
import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.availability.entity.TimeAvailabilityType;
import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.common.exception.InvalidTimeAvailabilityException;
import com.greedy.meetlink.common.validation.ParticipantValidator;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.entity.Participant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeAvailabilityService {
    private final TimeAvailabilityRepository timeAvailabilityRepository;
    private final ParticipantValidator participantValidator;

    @Transactional
    public void submit(String meetingCode, String token, TimeAvailabilityRequest request) {
        Participant participant =
                participantValidator.validateAndGetParticipant(meetingCode, token);
        Meeting meeting = participant.getMeeting();

        // 모임 타입 검증
        validateByMeetingType(meeting, request);

        // 기존 데이터 제거
        timeAvailabilityRepository.deleteByMeetingAndParticipant(meeting, participant);

        // 새 데이터 저장
        List<TimeAvailability> entities =
                request.getAvailabilities().stream()
                        .flatMap(
                                (daily) ->
                                        daily.getStartTimes().stream()
                                                .map(
                                                        (time) ->
                                                                TimeAvailability.create(
                                                                        meeting,
                                                                        participant,
                                                                        daily.getDate(),
                                                                        daily.getDayOfWeek(),
                                                                        time)))
                        .toList();

        timeAvailabilityRepository.saveAll(entities);

        // 시간 제출 여부 체크
        participant.markTimeSubmitted();
    }

    @Transactional(readOnly = true)
    public List<TimeAvailabilitiesResponse> getTimeAvailabilities(
            String meetingCode, String token) {
        Meeting meeting =
                participantValidator.validateAndGetParticipant(meetingCode, token).getMeeting();

        List<TimeAvailability> all = timeAvailabilityRepository.findByMeeting(meeting);

        Map<Long, List<TimeAvailability>> byParticipantId =
                all.stream().collect(Collectors.groupingBy(ta -> ta.getParticipant().getId()));

        return byParticipantId.values().stream()
                .map(
                        rows ->
                                TimeAvailabilitiesResponse.builder()
                                        .nickname(rows.getFirst().getParticipant().getNickname())
                                        .availabilities(buildEntries(rows))
                                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeAvailabilityResponse> getMyTimeAvailability(String meetingCode, String token) {
        Participant participant =
                participantValidator.validateAndGetParticipant(meetingCode, token);

        List<TimeAvailability> rows =
                timeAvailabilityRepository.findByMeetingAndParticipant(
                        participant.getMeeting(), participant);

        return buildEntries(rows);
    }

    private List<TimeAvailabilityResponse> buildEntries(List<TimeAvailability> rows) {
        Map<Object, List<TimeAvailability>> grouped =
                rows.stream()
                        .collect(
                                Collectors.groupingBy(
                                        row ->
                                                row.getDate() != null
                                                        ? row.getDate()
                                                        : row.getDayOfWeek()));

        return grouped.values().stream()
                .map(
                        group -> {
                            LocalDate date = group.getFirst().getDate();
                            Integer dayOfWeek = group.getFirst().getDayOfWeek();
                            List<LocalTime> times =
                                    group.stream()
                                            .map(TimeAvailability::getStartTime)
                                            .sorted()
                                            .toList();
                            return TimeAvailabilityResponse.builder()
                                    .date(date)
                                    .dayOfWeek(dayOfWeek)
                                    .startTimes(times)
                                    .build();
                        })
                .sorted(
                        (a, b) -> {
                            if (a.getDate() != null && b.getDate() != null)
                                return a.getDate().compareTo(b.getDate());
                            if (a.getDayOfWeek() != null && b.getDayOfWeek() != null)
                                return Integer.compare(a.getDayOfWeek(), b.getDayOfWeek());
                            return 0;
                        })
                .toList();
    }

    private void validateByMeetingType(Meeting meeting, TimeAvailabilityRequest request) {
        TimeAvailabilityType type = meeting.getTimeAvailabilityType();

        for (TimeAvailabilityRequest.DailyAvailability daily : request.getAvailabilities()) {
            if (type == TimeAvailabilityType.WEEKLY && daily.getDayOfWeek() == null) {
                throw new InvalidTimeAvailabilityException("요일 기반 모임은 요일 기반 입력만 가능합니다.");
            }

            if (type == TimeAvailabilityType.SPECIFIC_DATE && daily.getDate() == null) {
                throw new InvalidTimeAvailabilityException("날짜 기반 모임은 날짜 기반 입력만 가능합니다.");
            }
        }
    }
}
