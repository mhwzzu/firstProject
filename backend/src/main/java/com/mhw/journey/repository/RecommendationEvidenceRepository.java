package com.mhw.journey.repository;

import com.mhw.journey.model.RecommendationEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendationEvidenceRepository extends JpaRepository<RecommendationEvidence, Long> {
    List<RecommendationEvidence> findByCandidateIdOrderByDiscoveredAtDesc(Long candidateId);
}
