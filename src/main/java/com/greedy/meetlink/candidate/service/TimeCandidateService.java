package com.greedy.meetlink.candidate.service;

import com.greedy.meetlink.availability.entity.TimeAvailability;
import com.greedy.meetlink.availability.repository.TimeAvailabilityRepository;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateListResponse;
import com.greedy.meetlink.candidate.dto.response.TimeCandidateResponse;
import com.greedy.meetlink.candidate.entity.TimeCandidate;
import com.greedy.meetlink.candidate.repository.TimeCandidateRepository;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.participant.repository.ParticipantRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
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

    private final TimeCandidateRepository timeCandidateRepository;
    private final TimeAvailabilityRepository timeAvailabilityRepository;
    private final ParticipantRepository participantRepository;

    /** 모임 코드를 기반으로 추천 시간 후보를 계산, 저장 */
    @Transactional
    public TimeCandidateListResponse calculateAndSave(String code) {

        // 재계산 필요 여부 확인
        if (!isCalculationRequired(code)) {
            List<TimeCandidate> existingCandidates =
                    timeCandidateRepository.findByMeetingCode(code);

            List<TimeCandidateListResponse.HeatmapResponse> heatmapList =
                    generateHeatmap(timeAvailabilityRepository.findByMeetingCode(code));

            List<TimeCandidateResponse> rankingResponses =
                    existingCandidates.stream().map(TimeCandidateResponse::from).toList();

            return new TimeCandidateListResponse(heatmapList, rankingResponses);
        }

        timeCandidateRepository.deleteByMeetingCode(code);

        List<TimeAvailability> availabilities = timeAvailabilityRepository.findByMeetingCode(code);

        if (availabilities.isEmpty()) {
            return new TimeCandidateListResponse(Collections.emptyList(), Collections.emptyList());
        }

        Meeting meeting = availabilities.get(0).getParticipant().getMeeting();

        List<TimeCandidateListResponse.HeatmapResponse> heatmapList =
                generateHeatmap(availabilities);

        List<TimeCandidate> newCandidates = extractTopRankings(heatmapList, meeting);

        timeCandidateRepository.saveAll(newCandidates);

        List<TimeCandidateResponse> rankingResponses =
                newCandidates.stream().map(TimeCandidateResponse::from).toList();

        return new TimeCandidateListResponse(heatmapList, rankingResponses);
    }

    /** 재계산 필요 여부 판단 */
    private boolean isCalculationRequired(String code) {
        Optional<LocalDateTime> lastSubmission = participantRepository.findLastTimeSubmission(code);

        if (lastSubmission.isEmpty()) {
            return false;
        }

        Optional<LocalDateTime> lastCalculated =
                timeCandidateRepository.findLatestCreatedAtByMeetingCode(code);

        return lastCalculated
                .map((calculated) -> lastSubmission.get().isAfter(calculated))
                .orElse(true);
    }

    /** 히트맵 응답용 리스트로 변환 */
    private List<TimeCandidateListResponse.HeatmapResponse> generateHeatmap(
            List<TimeAvailability> availabilities) {
        Map<LocalDate, Map<LocalTime, Integer>> heatmapMap =
                availabilities.stream()
                        .collect(
                                Collectors.groupingBy(
                                        TimeAvailability::getDate,
                                        Collectors.groupingBy(
                                                TimeAvailability::getStartTime,
                                                Collectors.reducing(0, e -> 1, Integer::sum))));

        List<TimeCandidateListResponse.HeatmapResponse> heatmapResponses = new ArrayList<>();

        for (Map.Entry<LocalDate, Map<LocalTime, Integer>> dateEntry : heatmapMap.entrySet()) {
            LocalDate date = dateEntry.getKey();
            Integer dayOfWeek = date.getDayOfWeek().getValue() % 7;

            List<TimeCandidateListResponse.TimeSlot> slots = new ArrayList<>();
            for (Map.Entry<LocalTime, Integer> timeEntry : dateEntry.getValue().entrySet()) {
                slots.add(
                        new TimeCandidateListResponse.TimeSlot(
                                timeEntry.getKey(), timeEntry.getValue()));
            }

            slots.sort(Comparator.comparing(TimeCandidateListResponse.TimeSlot::startTime));
            heatmapResponses.add(
                    new TimeCandidateListResponse.HeatmapResponse(date, dayOfWeek, slots));
        }

        heatmapResponses.sort(
                Comparator.comparing(TimeCandidateListResponse.HeatmapResponse::date));
        return heatmapResponses;
    }

    /** 랭킹 추출 */
    private List<TimeCandidate> extractTopRankings(
            List<TimeCandidateListResponse.HeatmapResponse> heatmaps, Meeting meeting) {

        record SlotData(LocalDate date, Integer dayOfWeek, LocalTime startTime, int count) {}
        List<SlotData> allSlots = new ArrayList<>();

        for (TimeCandidateListResponse.HeatmapResponse heatmap : heatmaps) {
            for (TimeCandidateListResponse.TimeSlot slot : heatmap.slots()) {
                allSlots.add(
                        new SlotData(
                                heatmap.date(),
                                heatmap.dayOfWeek(),
                                slot.startTime(),
                                slot.availableCount()));
            }
        }

        // 1순위: 인원수, 2순위: 빠른 날짜, 3순위: 빠른 시간
        allSlots.sort(
                (a, b) -> {
                    if (a.count() != b.count()) return Integer.compare(b.count(), a.count());
                    if (!a.date().equals(b.date())) return a.date().compareTo(b.date());
                    return a.startTime().compareTo(b.startTime());
                });

        List<TimeCandidate> topRankings = new ArrayList<>();
        int rank = 1;
        int limit = Math.min(10, allSlots.size());

        for (int i = 0; i < limit; i++) {
            SlotData slot = allSlots.get(i);

            TimeCandidate candidate =
                    TimeCandidate.builder()
                            .meeting(meeting)
                            .date(slot.date())
                            .dayOfWeek(slot.dayOfWeek())
                            .startTime(slot.startTime())
                            .endTime(slot.startTime().plusMinutes(30))
                            .availableCount(slot.count())
                            .rank(rank++)
                            .build();

            topRankings.add(candidate);
        }

        return topRankings;
    }

    @Transactional(readOnly = true)
    public TimeCandidateListResponse getCandidates(String code) {
        List<TimeCandidate> candidates = timeCandidateRepository.findByMeetingCode(code);

        List<TimeAvailability> availabilities = timeAvailabilityRepository.findByMeetingCode(code);
        List<TimeCandidateListResponse.HeatmapResponse> heatmap = generateHeatmap(availabilities);

        List<TimeCandidateResponse> rankingResponses =
                candidates.stream().map(TimeCandidateResponse::from).toList();

        return new TimeCandidateListResponse(heatmap, rankingResponses);
    }
}
