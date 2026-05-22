package com.project.artconnect.modules.reviews.repository;

import com.project.artconnect.modules.reviews.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Page<Review> findByArtistIdOrderByCreatedAtDesc(UUID artistId, Pageable pageable);

    Optional<Review> findByOrderId(UUID orderId);

    boolean existsByOrderId(UUID orderId);

    Page<Review> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.artist.id = :artistId")
    Double findAverageRatingByArtistId(@Param("artistId") UUID artistId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.artist.id = :artistId")
    Long findReviewCountByArtistId(@Param("artistId") UUID artistId);
}

