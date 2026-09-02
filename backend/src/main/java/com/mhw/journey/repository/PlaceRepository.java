package com.mhw.journey.repository;
import com.mhw.journey.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PlaceRepository extends JpaRepository<Place, Long> { Optional<Place> findBySourceAndExternalId(String source, String externalId); }
