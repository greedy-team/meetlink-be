package com.greedy.meetlink.candidate.repository;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import com.greedy.meetlink.candidate.entity.PlaceCandidateRoute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCandidateRouteRepository extends JpaRepository<PlaceCandidateRoute, Long> {
    List<PlaceCandidateRoute> findByPlaceCandidateIn(List<PlaceCandidate> candidates);
}
