package com.greedy.meetlink.candidate.service;

import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.availability.repository.projection.TimeAvailabilityHeatmapRow;
import com.greedy.meetlink.candidate.dto.response.TimeCandidatesResponse;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
    public TimeCandidatesResponse calculate(String code) {
        if (!isCalculationRequired(code)) return get(code);

        // DB에서 이미 집계 완료된 결과만 가져옴
        List<TimeAvailabilityHeatmapRow> rows = timeAvailabilityRepository.findHeatmapByMeetingCode(code);

        // 데이터 없으면 후보 제거 후 빈 응답
        if (rows.isEmpty()) {
            timeCandidateRepository.deleteByMeetingCode(code);
            return new TimeCandidatesResponse(List.of(), List.of());
        }

        Meeting meeting = meetingRepository.findByCode(code).orElseThrow(MeetingNotFoundException::new);

        // 기존 후보 삭제
        timeCandidateRepository.deleteByMeetingCode(code);

        // 상위 랭킹 후보 생성 후 연속된 슬롯 합침
        List<TimeCandidate> candidates = buildCandidates(rows, meeting);

        timeCandidateRepository.saveAll(candidates);

        return new TimeCandidatesResponse(
                toHeatmap(rows),
                candidates.stream().map(TimeCandidateResponse::from).toList());
    }

    @Transactional(readOnly = true)
    public TimeCandidatesResponse get(String code) {
        List<TimeCandidate> candidates = timeCandidateRepository.findByMeeting_CodeOrderByRankAsc(code);
        List<TimeAvailabilityHeatmapRow> rows = timeAvailabilityRepository.findHeatmapByMeetingCode(code);

        return new TimeCandidatesResponse(toHeatmap(rows), candidates.stream().map(TimeCandidateResponse::from).toList());
    }

    /** 재계산 필요 여부 판단 */
    private boolean isCalculationRequired(String code) {
        Optional<LocalDateTime> lastSubmission = participantRepository.findLastTimeSubmission(code);

        if (lastSubmission.isEmpty()) return false;

        Optional<LocalDateTime> lastCalculated =
                timeCandidateRepository.findLastCalculatedAt(code);

        // 계산된 후보가 없거나, 마지막 제출이 마지막 계산보다 이후면 재계산 필요
        return lastCalculated
                .map((calculated) -> lastSubmission.get().isAfter(calculated))
                .orElse(true);
    }

    /** DB aggregation 결과 -> 후보 상위 N개 생성 */
    private List<TimeCandidate> buildCandidates(List<TimeAvailabilityHeatmapRow> rows, Meeting meeting) {
        if (rows.isEmpty()) {
            return List.of();
        }

        List<TimeCandidate> mergedCandidates = mergeConsecutiveSlots(rows, meeting);

        mergedCandidates.sort(Comparator
                .comparing(TimeCandidate::getAvailableCount, Comparator.reverseOrder())
                .thenComparing(TimeCandidate::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(TimeCandidate::getStartTime));

        List<TimeCandidate> ranked = mergedCandidates.stream()
                .limit(MAX_CANDIDATES)
                .toList();

        for (int i = 0; i < ranked.size(); i++) {
            ranked.get(i).assignRank(i + 1);
        }

        return ranked;
    }

    private List<TimeCandidate> mergeConsecutiveSlots(List<TimeAvailabilityHeatmapRow> rows, Meeting meeting) {
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
                merged.add(TimeCandidate.builder()
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

        merged.add(TimeCandidate.builder()
                .meeting(meeting)
                .date(candidateDate)
                .dayOfWeek(candidateDayOfWeek)
                .startTime(candidateStart)
                .endTime(candidateEnd)
                .availableCount(candidateAvailableCount)
                .build());

        return merged;
    }

    /** DB aggregation 결과를 Heatmap 형태로 변환 */
    private List<TimeCandidatesResponse.Heatmap> toHeatmap(List<TimeAvailabilityHeatmapRow> rows) {
        Map<Object, List<TimeAvailabilityHeatmapRow>> grouped = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> row.getDate() != null ? row.getDate() : row.getDayOfWeek()
                ));

        List<TimeCandidatesResponse.Heatmap> result = grouped.values().stream()
                .map((groupRows) -> {
                    LocalDate date = groupRows.getFirst().getDate();
                    Integer dayOfWeek = groupRows.getFirst().getDayOfWeek();
                    List<TimeCandidatesResponse.TimeSlot> slots = groupRows.stream()
                            .map(row -> new TimeCandidatesResponse.TimeSlot(
                                    row.getStartTime(), row.getAvailableCount()))
                            .sorted(Comparator.comparing(TimeCandidatesResponse.TimeSlot::startTime))
                            .toList();
                    return new TimeCandidatesResponse.Heatmap(date, dayOfWeek, slots);
                }).sorted((a, b) -> {
                    if (a.date() != null && b.date() != null)
                        return a.date().compareTo(b.date());
                    if (a.dayOfWeek() != null && b.dayOfWeek() != null)
                        return Integer.compare(a.dayOfWeek(), b.dayOfWeek());
                    return 0;
                }).collect(Collectors.toCollection(ArrayList::new));

        return result;
    }
}
