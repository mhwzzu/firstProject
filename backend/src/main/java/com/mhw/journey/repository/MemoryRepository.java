package com.mhw.journey.repository;

import com.mhw.journey.model.Memory;
import com.mhw.journey.model.MemoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoryRepository extends JpaRepository<Memory, Long> {
    List<Memory> findAllByOrderByVisitedAtDesc();
    long countByType(MemoryType type);
}

