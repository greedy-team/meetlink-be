package com.greedy.meetlink.result.mapper;

import com.greedy.meetlink.availability.TimeAvailability;
import com.greedy.meetlink.candidate.TimeCandidate;
import com.greedy.meetlink.result.dto.TimeRecommendationResponse;
import com.greedy.meetlink.result.dto.TimeRecommendationResponse.HeatmapResponse;
import com.greedy.meetlink.result.dto.TimeRecommendationResponse.TimeCandidateResponse;
import com.greedy.meetlink.result.dto.TimeRecommendationResponse.TimeSlotResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TimeRecommendationResponse 변환 전담 Mapper
 *
 * <p>[분리 이유] 기존에 TimeRecommendationResponse 내부 정적 메서드로 존재하던 변환 로직을 이곳으로 이동. - DTO는 데이터 구조 정의만 담당
 * (SRP 준수) - 변환 로직의 테스트 및 유지보수 용이성 향상
 */
public class TimeRecommendationMapper {

    private TimeRecommendationMapper() {
        // 인스턴스 생성 방지 (유틸리티 클래스)
    }

    /** TimeAvailability + TimeCandidate 리스트로 전체 응답 생성 */
    public static TimeRecommendationResponse toResponse(
            List<TimeAvailability> availabilities, List<TimeCandidate> candidates) {
        return TimeRecommendationResponse.builder()
                .heatmaps(toHeatmapResponses(availabilities))
                .candidates(toCandidateResponses(candidates))
                .build();
    }

    /** TimeAvailability 리스트 → 히트맵 응답 변환 - SPECIFIC_DATE 모드: 날짜 기준 그룹핑 - WEEKLY 모드: 요일 기준 그룹핑 */
    private static List<HeatmapResponse> toHeatmapResponses(List<TimeAvailability> availabilities) {
        if (availabilities == null || availabilities.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Object, List<TimeAvailability>> grouped =
                availabilities.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ta ->
                                                ta.getDate() != null
                                                        ? ta.getDate()
                                                        : ta.getDayOfWeek()));

        return grouped.entrySet().stream()
                .map(entry -> toHeatmapResponse(entry.getKey(), entry.getValue()))
                .sorted(TimeRecommendationMapper::compareHeatmaps)
                .collect(Collectors.toList());
    }

    private static HeatmapResponse toHeatmapResponse(
            Object key, List<TimeAvailability> groupedAvailabilities) {
        List<TimeSlotResponse> timeSlots = toTimeSlotResponses(groupedAvailabilities);

        if (key instanceof LocalDate date) {
            return HeatmapResponse.builder()
                    .date(date)
                    .dayOfWeek(null)
                    .timeSlots(timeSlots)
                    .build();
        } else {
            return HeatmapResponse.builder()
                    .date(null)
                    .dayOfWeek((Integer) key)
                    .timeSlots(timeSlots)
                    .build();
        }
    }

    private static List<TimeSlotResponse> toTimeSlotResponses(
            List<TimeAvailability> availabilities) {
        Map<LocalTime, Long> timeSlotCounts =
                availabilities.stream()
                        .collect(
                                Collectors.groupingBy(
                                        TimeAvailability::getStartTime, Collectors.counting()));

        return timeSlotCounts.entrySet().stream()
                .map(
                        entry ->
                                TimeSlotResponse.builder()
                                        .startTime(entry.getKey())
                                        .availableCount(entry.getValue().intValue())
                                        .build())
                .sorted(Comparator.comparing(TimeSlotResponse::getStartTime))
                .collect(Collectors.toList());
    }

    /** TimeCandidate 리스트 → 추천 후보 응답 변환 */
    private static List<TimeCandidateResponse> toCandidateResponses(
            List<TimeCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        return candidates.stream()
                .sorted(Comparator.comparingInt(TimeCandidate::getRank))
                .map(
                        tc ->
                                TimeCandidateResponse.builder()
                                        .id(tc.getId())
                                        .rank(tc.getRank())
                                        .date(tc.getDate())
                                        .dayOfWeek(tc.getDayOfWeek())
                                        .startTime(tc.getStartTime())
                                        .endTime(tc.getEndTime())
                                        .availableCount(tc.getAvailableCount())
                                        .build())
                .collect(Collectors.toList());
    }

    private static int compareHeatmaps(HeatmapResponse h1, HeatmapResponse h2) {
        if (h1.getDate() != null && h2.getDate() != null) {
            return h1.getDate().compareTo(h2.getDate());
        }
        if (h1.getDayOfWeek() != null && h2.getDayOfWeek() != null) {
            return h1.getDayOfWeek().compareTo(h2.getDayOfWeek());
        }
        return 0;
    }
}
