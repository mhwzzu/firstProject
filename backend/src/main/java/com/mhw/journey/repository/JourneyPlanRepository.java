package com.mhw.journey.repository;
import com.mhw.journey.model.JourneyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface JourneyPlanRepository extends JpaRepository<JourneyPlan, Long> { List<JourneyPlan> findBySpaceIdOrderByCreatedAtDesc(Long spaceId); }
