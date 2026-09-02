package com.mhw.journey.repository;
import com.mhw.journey.model.PreferenceProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PreferenceProfileRepository extends JpaRepository<PreferenceProfile, Long> {
    Optional<PreferenceProfile> findBySpaceIdAndUserId(Long spaceId, Long userId);
    List<PreferenceProfile> findBySpaceId(Long spaceId);
}
