package com.mhw.journey.repository;
import com.mhw.journey.model.CandidateFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CandidateFeedbackRepository extends JpaRepository<CandidateFeedback, Long> {
    Optional<CandidateFeedback> findByCandidateIdAndUserId(Long candidateId, Long userId);
    List<CandidateFeedback> findByUserId(Long userId);
}
