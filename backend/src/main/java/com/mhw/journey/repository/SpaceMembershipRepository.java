package com.mhw.journey.repository;
import com.mhw.journey.model.SpaceMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface SpaceMembershipRepository extends JpaRepository<SpaceMembership, Long> {
    Optional<SpaceMembership> findFirstByUserId(Long userId);
    Optional<SpaceMembership> findBySpaceIdAndUserId(Long spaceId, Long userId);
    List<SpaceMembership> findBySpaceIdOrderByJoinedAtAsc(Long spaceId);
}
