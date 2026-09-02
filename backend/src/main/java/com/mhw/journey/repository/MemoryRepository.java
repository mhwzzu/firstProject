package com.mhw.journey.repository;

import com.mhw.journey.model.Memory;
import com.mhw.journey.model.MemoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoryRepository extends JpaRepository<Memory, Long> {
    List<Memory> findAllBySpaceIdOrderByVisitedAtDesc(Long spaceId);
    List<Memory> findBySpaceIdAndCategoryIgnoreCaseOrderByVisitedAtDesc(Long spaceId, String category);
    long countBySpaceIdAndType(Long spaceId, MemoryType type);
}
