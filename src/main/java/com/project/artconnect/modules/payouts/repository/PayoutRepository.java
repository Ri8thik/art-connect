package com.project.artconnect.modules.payouts.repository;

import com.project.artconnect.modules.payouts.entity.Payout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    Page<Payout> findByArtistIdOrderByCreatedAtDesc(UUID artistId, Pageable pageable);
}

