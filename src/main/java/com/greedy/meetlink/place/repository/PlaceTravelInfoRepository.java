package com.greedy.meetlink.place.repository;

import com.greedy.meetlink.candidate.PlaceCandidate;
import com.greedy.meetlink.place.domain.PlaceTravelInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceTravelInfoRepository extends JpaRepository<PlaceTravelInfo, Long> {

    /**
     * 특정 장소 후보의 참여자별 이동시간 목록 조회
     * participant를 fetch join하여 N+1 방지
     */
    @Query("SELECT pti FROM PlaceTravelInfo pti " +
            "JOIN FETCH pti.participant " +
            "WHERE pti.placeCandidate = :candidate")
    List<PlaceTravelInfo> findByPlaceCandidateWithParticipant(
            @Param("candidate") PlaceCandidate candidate);

    /**
     * 여러 장소 후보의 이동시간 일괄 조회 (IN 쿼리)
     * getCandidates()에서 후보 3개를 한 번에 조회할 때 사용
     */
    @Query("SELECT pti FROM PlaceTravelInfo pti " +
            "JOIN FETCH pti.participant " +
            "WHERE pti.placeCandidate IN :candidates")
    List<PlaceTravelInfo> findByPlaceCandidatesWithParticipant(
            @Param("candidates") List<PlaceCandidate> candidates);
}
