package com.greedy.meetlink.candidate;

import com.greedy.meetlink.candidate.entity.PlaceCandidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceCandidateRepository extends JpaRepository<PlaceCandidate, Long> {}
