package com.mhw.journey.repository;
import com.mhw.journey.model.RecommendationCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface RecommendationCandidateRepository extends JpaRepository<RecommendationCandidate, Long> { List<RecommendationCandidate> findByRunIdOrderByScoreDesc(Long runId); }
