package com.greedy.meetlink.result.dto;

import com.greedy.meetlink.availability.TimeAvailability;
import com.greedy.meetlink.candidate.TimeCandidate;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Builder
public class TimeRecommendationResponse {

    /** 1. 참여자 전체 히트맵 정보 (날짜/요일별 가용 인원 현황) */
    private List<HeatmapResponse> heatmaps;

    /** 2. 추천 순위 리스트 (알고리즘에 의해 선정된 후보군) */
    private List<TimeCandidateResponse> candidates;

    /** 히트맵 상위 그룹 (날짜별) */
    @Getter
    @Builder
    public static class HeatmapResponse {
        private LocalDate date;            // 날짜 (SPECIFIC_DATE 모드일 때 사용)
        private Integer dayOfWeek;         // 요일 (WEEKLY 모드일 때 사용, 0~6)
        private List<TimeSlotResponse> timeSlots; // 해당 날짜의 시간 리스트
    }

    /** 히트맵 하위 슬롯 (시간별 인원) */
    @Getter
    @Builder
    public static class TimeSlotResponse {
        private LocalTime startTime;       // 시작 시간 (ex. 10:00)
        private Integer availableCount;    // 해당 시간에 가능한 참여자 수
    }

    /** 추천 순위 리스트 항목 */
    @Getter
    @Builder
    public static class TimeCandidateResponse {
        private Long id;                   // TimeCandidate ID
        private Integer rank;              // 추천 순위 (1, 2, 3...)
        private LocalDate date;            // 날짜
        private Integer dayOfWeek;         // 요일
        private LocalTime startTime;       // 시작 시간
        private LocalTime endTime;         // 종료 시간
        private Integer availableCount;    // 가능 인원
    }

    /**
     * TimeCandidate 리스트로부터 candidates 생성
     */
    public static List<TimeCandidateResponse> fromTimeCandidates(List<TimeCandidate> timeCandidates) {
        if (timeCandidates == null || timeCandidates.isEmpty()) {
            return Collections.emptyList();
        }

        return timeCandidates.stream()
                .sorted(Comparator.comparingInt(TimeCandidate::getRank))
                .map(tc -> TimeCandidateResponse.builder()
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

    /**
     * TimeAvailability 리스트로부터 히트맵 생성
     */
    public static List<HeatmapResponse> fromTimeAvailabilities(List<TimeAvailability> availabilities) {
        if (availabilities == null || availabilities.isEmpty()) {
            return Collections.emptyList();
        }

        // 날짜/요일별로 그룹핑
        Map<Object, List<TimeAvailability>> grouped = availabilities.stream()
                .collect(Collectors.groupingBy(ta -> {
                    if (ta.getDate() != null) {
                        return ta.getDate(); // SPECIFIC_DATE 모드
                    } else {
                        return ta.getDayOfWeek(); // WEEKLY 모드
                    }
                }));

        return grouped.entrySet().stream()
                .map(entry -> {
                    Object key = entry.getKey();
                    List<TimeAvailability> groupedAvailabilities = entry.getValue();

                    // 시간별로 그룹핑하여 인원 카운트
                    Map<LocalTime, Long> timeSlotCounts = groupedAvailabilities.stream()
                            .collect(Collectors.groupingBy(
                                    TimeAvailability::getStartTime,
                                    Collectors.counting()
                            ));

                    List<TimeSlotResponse> timeSlots = timeSlotCounts.entrySet().stream()
                            .map(slotEntry -> TimeSlotResponse.builder()
                                    .startTime(slotEntry.getKey())
                                    .availableCount(slotEntry.getValue().intValue())
                                    .build())
                            .sorted(Comparator.comparing(TimeSlotResponse::getStartTime))
                            .collect(Collectors.toList());

                    // HeatmapResponse 생성
                    if (key instanceof LocalDate) {
                        return HeatmapResponse.builder()
                                .date((LocalDate) key)
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
                })
                .sorted((h1, h2) -> {
                    // 날짜 기준 정렬 또는 요일 기준 정렬
                    if (h1.getDate() != null && h2.getDate() != null) {
                        return h1.getDate().compareTo(h2.getDate());
                    } else if (h1.getDayOfWeek() != null && h2.getDayOfWeek() != null) {
                        return h1.getDayOfWeek().compareTo(h2.getDayOfWeek());
                    }
                    return 0;
                })
                .collect(Collectors.toList());
    }

    /**
     * TimeAvailability와 TimeCandidate 리스트로 전체 응답 생성
     */
    public static TimeRecommendationResponse of(
            List<TimeAvailability> availabilities,
            List<TimeCandidate> candidates) {
        return TimeRecommendationResponse.builder()
                .heatmaps(fromTimeAvailabilities(availabilities))
                .candidates(fromTimeCandidates(candidates))
                .build();
    }
}
