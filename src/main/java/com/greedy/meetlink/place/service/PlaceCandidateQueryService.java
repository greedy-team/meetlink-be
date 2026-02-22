package com.greedy.meetlink.place.service;

import com.greedy.meetlink.candidate.PlaceCandidateRepository;
import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.meeting.entity.Meeting;
import com.greedy.meetlink.place.client.dto.PlaceCandidateListResponse;
import com.greedy.meetlink.place.client.dto.RecommendedPlaceResponse;
import com.greedy.meetlink.place.domain.PlaceTravelInfo;
import com.greedy.meetlink.place.repository.PlaceTravelInfoRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추천 장소 조회 전용 서비스
 *
 * <p>PlaceCandidateController.getCandidates()의 조회 로직을 컨트롤러에서 분리하여 서비스 레이어로 이동.
 *
 * <p>[N+1 방지 전략] 1. PlaceCandidate 목록 1번 조회 2. PlaceTravelInfo 전체를 IN 쿼리로 1번 조회 (fetch join으로
 * Participant 포함) 3. candidateId 기준으로 groupingBy → 메모리에서 매핑 총 2번의 쿼리로 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceCandidateQueryService {

    private final PlaceCandidateRepository placeCandidateRepository;
    private final PlaceTravelInfoRepository placeTravelInfoRepository;

    public PlaceCandidateListResponse getCandidates(Meeting meeting) {
        // 1. 해당 미팅의 장소 후보 전체 조회 (rank 순)
        List<PlaceCandidate> candidates =
                placeCandidateRepository.findByMeetingOrderByRankAsc(meeting);

        if (candidates.isEmpty()) {
            return PlaceCandidateListResponse.of(List.of());
        }

        // 2. 참여자별 이동시간 전체를 IN 쿼리 1번으로 조회 (N+1 방지)
        List<PlaceTravelInfo> allTravelInfos =
                placeTravelInfoRepository.findByPlaceCandidatesWithParticipant(candidates);

        // 3. candidateId 기준으로 그룹핑
        Map<Long, List<PlaceTravelInfo>> travelInfoMap =
                allTravelInfos.stream()
                        .collect(Collectors.groupingBy(pti -> pti.getPlaceCandidate().getId()));

        // 4. 응답 조립
        List<RecommendedPlaceResponse> recommendedPlaces =
                candidates.stream()
                        .map(
                                candidate ->
                                        RecommendedPlaceResponse.of(
                                                candidate,
                                                travelInfoMap.getOrDefault(
                                                        candidate.getId(), List.of())))
                        .toList();

        return PlaceCandidateListResponse.of(recommendedPlaces);
    }
}
