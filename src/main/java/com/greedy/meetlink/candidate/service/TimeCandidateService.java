package com.greedy.meetlink.candidate.service;

import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.availability.repository.projection.TimeAvailabilityHeatmapRow;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.candidate.entity.TimeCandidate;
import com.greedy.meetlink.candidate.repository.TimeCandidateRepository;
import com.greedy.meetlink.common.exception.MeetingNotFoundException;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.meeting.repository.MeetingRepository;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeCandidateService {
    private static final int SLOT_MINUTES = 30;
    private static final int MAX_CANDIDATES = 10;

    private final TimeCandidateRepository timeCandidateRepository;
    private final TimeAvailabilityRepository timeAvailabilityRepository;
    private final ParticipantRepository participantRepository;
    private final MeetingRepository meetingRepository;

    @Transactional
    public List<TimeCandidateResponse> calculate(String code) {
        Meeting meeting =
                meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        if (!isCalculationRequired(meeting)) return list(code);

        List<TimeAvailabilityHeatmapRow> rows =
                timeAvailabilityRepository.findHeatmapByMeetingCode(
                        code, meeting.getTimeRangeStart(), meeting.getTimeRangeEnd());

        if (rows.isEmpty()) {
            timeCandidateRepository.deleteByMeetingCode(code);
            return List.of();
        }

        // 기존 후보 삭제
        timeCandidateRepository.deleteByMeetingCode(code);

        // 상위 랭킹 후보 생성 후 연속된 슬롯 합침
        List<TimeCandidate> candidates = buildCandidates(rows, meeting);

        timeCandidateRepository.saveAll(candidates);

        return candidates.stream().map(TimeCandidateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<TimeCandidateResponse> list(String code) {
        return timeCandidateRepository.findByMeeting_CodeOrderByRankAsc(code).stream()
                .map(TimeCandidateResponse::from)
                .toList();
    }

    private boolean isCalculationRequired(Meeting meeting) {
        Optional<LocalDateTime> lastSubmission =
                participantRepository.findLastTimeSubmission(meeting.getCode());

        if (lastSubmission.isEmpty()) return false;

        Optional<LocalDateTime> lastCalculated =
                timeCandidateRepository.findLastCalculatedAt(meeting.getCode());

        if (lastCalculated.isEmpty()) return true;

        LocalDateTime calculated = lastCalculated.get();

        // 마지막 제출 또는 모임 설정 변경이 마지막 계산 이후이면 재계산
        return lastSubmission.get().isAfter(calculated)
                || meeting.getUpdatedAt().isAfter(calculated);
    }

    /** DB aggregation 결과 -> 후보 상위 N개 생성 */
    private List<TimeCandidate> buildCandidates(
            List<TimeAvailabilityHeatmapRow> rows, Meeting meeting) {
        if (rows.isEmpty()) {
            return List.of();
        }

        List<TimeCandidate> mergedCandidates = mergeConsecutiveSlots(rows, meeting);

        mergedCandidates.sort(
                Comparator.comparing(TimeCandidate::getAvailableCount, Comparator.reverseOrder())
                        .thenComparing(
                                TimeCandidate::getDate,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(TimeCandidate::getStartTime));

        List<TimeCandidate> ranked = mergedCandidates.stream().limit(MAX_CANDIDATES).toList();

        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).assignRank(i + 1);
        }

        return ranked;
    }

    private List<TimeCandidate> mergeConsecutiveSlots(
            List<TimeAvailabilityHeatmapRow> rows, Meeting meeting) {
        List<TimeCandidate> merged = new ArrayList<>();

        LocalDate candidateDate = rows.getFirst().getDate();
        Integer candidateDayOfWeek = rows.getFirst().getDayOfWeek();
        LocalTime candidateStart = rows.getFirst().getStartTime();
        LocalTime candidateEnd = candidateStart.plusMinutes(SLOT_MINUTES);
        int candidateAvailableCount = Math.toIntExact(rows.getFirst().getAvailableCount());

        for (int i = 1; i < rows.size(); i++) {
            TimeAvailabilityHeatmapRow row = rows.get(i);
            boolean consecutive = candidateEnd.equals(row.getStartTime());
            boolean sameCount = candidateAvailableCount == row.getAvailableCount();

            if (consecutive && sameCount) {
                candidateEnd = candidateEnd.plusMinutes(SLOT_MINUTES);
            } else {
                merged.add(
                        TimeCandidate.builder()
                                .meeting(meeting)
                                .date(candidateDate)
                                .dayOfWeek(candidateDayOfWeek)
                                .startTime(candidateStart)
                                .endTime(candidateEnd)
                                .availableCount(candidateAvailableCount)
                                .build());

                candidateDate = row.getDate();
                candidateDayOfWeek = row.getDayOfWeek();
                candidateStart = row.getStartTime();
                candidateEnd = candidateStart.plusMinutes(SLOT_MINUTES);
                candidateAvailableCount = Math.toIntExact(row.getAvailableCount());
            }
        }

        merged.add(
                TimeCandidate.builder()
                        .meeting(meeting)
                        .date(candidateDate)
                        .dayOfWeek(candidateDayOfWeek)
                        .startTime(candidateStart)
                        .endTime(candidateEnd)
                        .availableCount(candidateAvailableCount)
                        .build());

        return merged;
    }
}
