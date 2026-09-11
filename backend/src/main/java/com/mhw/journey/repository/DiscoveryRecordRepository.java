package com.mhw.journey.repository;

import com.mhw.journey.model.DiscoveryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface DiscoveryRecordRepository extends JpaRepository<DiscoveryRecord, Long> {
    List<DiscoveryRecord> findBySpaceIdAndQueryKeyAndDiscoveredAtAfterOrderByDiscoveredAtDesc(Long spaceId, String queryKey, LocalDateTime after);
}
