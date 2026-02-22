package com.greedy.meetlink.candidate.repository;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.entity.PlaceTravelInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceTravelInfoRepository extends JpaRepository<PlaceTravelInfo, Long> {

    @Query(
            "SELECT pti FROM PlaceTravelInfo pti "
                    + "JOIN FETCH pti.participant "
                    + "WHERE pti.placeCandidate IN :candidates")
    List<PlaceTravelInfo> findByPlaceCandidatesWithParticipant(
            @Param("candidates") List<PlaceCandidate> candidates);
}
