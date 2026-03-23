package com.greedy.meetlink.candidate.service;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.candidate.entity.TimeCandidate;
import com.greedy.meetlink.candidate.repository.TimeCandidateRepository;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.common.validation.ParticipantValidator;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import com.greedy.meetlink.result.entity.MeetingResult;
import com.greedy.meetlink.result.repository.MeetingResultRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeCandidateService {
    private static final int SLOT_MINUTES = 30;
    private static final int MAX_CANDIDATES = 10;

    private final TimeCandidateRepository timeCandidateRepository;
    private final TimeAvailabilityRepository timeAvailabilityRepository;
    private final ParticipantRepository participantRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingResultRepository meetingResultRepository;
    private final ParticipantValidator participantValidator;

    @Transactional
    public void calculateTimeCandidates(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        if (!isCalculationRequired(meeting)) {
            log.debug("Time candidate calculation skipped: meeting={}", code);
            return;
        }

        doCalculate(code, meeting);
    }

    @Transactional
    public void recalculateOnLeave(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);
        doCalculate(code, meeting);
    }

    private void doCalculate(String code, Meeting meeting) {
        log.info("Time candidate calculation started: meeting={}", code);

        timeCandidateRepository.deleteByMeeting(meeting);

        List<TimeCandidate> candidates = buildCandidates(meeting);

        if (candidates.isEmpty()) {
            log.info("No time candidates found: meeting={}", code);
            return;
        }

        timeCandidateRepository.saveAll(candidates);
        updateMeetingResult(meeting, candidates);

        log.info(
                "Time candidate calculation completed: meeting={}, results={}",
                code,
                candidates.size());
    }

    @Transactional(readOnly = true)
    public List<TimeCandidateResponse> getTimeCandidates(String code, String token) {
        Meeting meeting = participantValidator.validateAndGetParticipant(code, token).getMeeting();
        return toResponses(meeting);
    }

    private List<TimeCandidateResponse> toResponses(Meeting meeting) {
        LocalDateTime now = LocalDateTime.now();

        return timeCandidateRepository.findByMeetingOrderByRankAsc(meeting).stream()
                .filter(
                        c ->
                                c.getDate() == null
                                        || LocalDateTime.of(c.getDate(), c.getStartTime())
                                                .isAfter(now))
                .map(TimeCandidateResponse::from)
                .toList();
    }

    private void updateMeetingResult(Meeting meeting, List<TimeCandidate> candidates) {
        MeetingResult result = meetingResultRepository.findByMeeting(meeting).orElseThrow();
        candidates.stream()
                .filter(c -> c.getRank() == 1)
                .findFirst()
                .ifPresent(result::updateTimeCandidate);
    }

    private boolean isCalculationRequired(Meeting meeting) {
        if (meeting.getTimeRangeStart() == null || meeting.getTimeRangeEnd() == null) return false;

        Optional<LocalDateTime> lastSubmission =
                participantRepository.findLastTimeSubmission(meeting.getCode());

        if (lastSubmission.isEmpty()) return false;

        Optional<LocalDateTime> lastCalculated =
                timeCandidateRepository.findLastCalculatedAt(meeting);

        if (lastCalculated.isEmpty()) return true;

        LocalDateTime calculated = lastCalculated.get();

        // 마지막 제출 또는 모임 설정 변경이 마지막 계산 이후이면 재계산
        return lastSubmission.get().isAfter(calculated)
                || meeting.getUpdatedAt().isAfter(calculated);
    }

    /** DB aggregation 결과 -> 후보 상위 N개 생성 */
    private List<TimeCandidate> buildCandidates(Meeting meeting) {
        List<TimeAvailability> availabilities =
                timeAvailabilityRepository.findByMeetingCodeInTimeRange(
                        meeting.getCode(), meeting.getTimeRangeStart(), meeting.getTimeRangeEnd());

        if (availabilities.isEmpty()) {
            return List.of();
        }

        List<TimeCandidate> mergedCandidates = mergeConsecutiveSlots(availabilities, meeting);

        mergedCandidates.sort(
                Comparator.comparing(TimeCandidate::getAvailableCount, Comparator.reverseOrder())
                        .thenComparing(
                                TimeCandidate::getDate,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(
                                TimeCandidate::getDayOfWeek,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TimeCandidate::getStartTime));

        List<TimeCandidate> ranked = mergedCandidates.stream().limit(MAX_CANDIDATES).toList();

        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).assignRank(i + 1);
        }

        return ranked;
    }

    private List<TimeCandidate> mergeConsecutiveSlots(
            List<TimeAvailability> rawList, Meeting meeting) {
        rawList.sort(
                Comparator.comparing(
                                TimeAvailability::getDate,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(
                                TimeAvailability::getDayOfWeek,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TimeAvailability::getStartTime));

        Map<TimeSlot, Set<Long>> grouped = new LinkedHashMap<>();
        for (TimeAvailability ta : rawList) {
            TimeSlot slot = new TimeSlot(ta.getDate(), ta.getDayOfWeek(), ta.getStartTime());
            grouped.computeIfAbsent(slot, k -> new HashSet<>()).add(ta.getParticipant().getId());
        }

        if (grouped.isEmpty()) {
            return new ArrayList<>();
        }

        List<TimeCandidate> merged = new ArrayList<>();
        Iterator<Map.Entry<TimeSlot, Set<Long>>> iterator = grouped.entrySet().iterator();
        Map.Entry<TimeSlot, Set<Long>> firstEntry = iterator.next();

        LocalDate candidateDate = firstEntry.getKey().date();
        Integer candidateDayOfWeek = firstEntry.getKey().dayOfWeek();
        LocalTime candidateStart = firstEntry.getKey().startTime();
        LocalTime candidateEnd = candidateStart.plusMinutes(SLOT_MINUTES);
        Set<Long> candidateParticipants = firstEntry.getValue();

        while (iterator.hasNext()) {
            Map.Entry<TimeSlot, Set<Long>> entry = iterator.next();
            TimeSlot currentSlot = entry.getKey();
            Set<Long> currentParticipants = entry.getValue();

            boolean consecutive = candidateEnd.equals(currentSlot.startTime());
            boolean sameDay =
                    Objects.equals(candidateDayOfWeek, currentSlot.dayOfWeek())
                            && Objects.equals(candidateDate, currentSlot.date());

            boolean sameParticipants = candidateParticipants.equals(currentParticipants);

            if (consecutive && sameDay && sameParticipants) {
                candidateEnd = candidateEnd.plusMinutes(SLOT_MINUTES);
            } else {
                merged.add(
                        TimeCandidate.create(
                                meeting,
                                candidateDate,
                                candidateDayOfWeek,
                                candidateStart,
                                candidateEnd,
                                candidateParticipants.size()));

                candidateDate = currentSlot.date();
                candidateDayOfWeek = currentSlot.dayOfWeek();
                candidateStart = currentSlot.startTime();
                candidateEnd = candidateStart.plusMinutes(SLOT_MINUTES);
                candidateParticipants = currentParticipants;
            }
        }

        merged.add(
                TimeCandidate.create(
                        meeting,
                        candidateDate,
                        candidateDayOfWeek,
                        candidateStart,
                        candidateEnd,
                        candidateParticipants.size()));

        return merged;
    }

    private record TimeSlot(LocalDate date, Integer dayOfWeek, LocalTime startTime) {}
}
