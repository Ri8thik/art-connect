package com.project.artconnect.modules.artists.repository;

import com.project.artconnect.modules.artists.entity.ArtistProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArtistProfileRepository extends JpaRepository<ArtistProfile, UUID> {

    Optional<ArtistProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    Page<ArtistProfile> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT a FROM ArtistProfile a WHERE a.isActive = true " +
           "AND (:category IS NULL OR :category MEMBER OF a.categories) " +
           "AND (:city IS NULL OR a.user.locationCity = :city) " +
           "AND (:country IS NULL OR a.user.locationCountry = :country)")
    Page<ArtistProfile> searchArtists(
            @Param("category") String category,
            @Param("city") String city,
            @Param("country") String country,
            Pageable pageable
    );
}

