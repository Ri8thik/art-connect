package com.project.artconnect.modules.earnings.repository;

import com.project.artconnect.common.enums.EarningStatus;
import com.project.artconnect.modules.earnings.entity.Earning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface EarningRepository extends JpaRepository<Earning, UUID> {

    Page<Earning> findByArtistIdOrderByCreatedAtDesc(UUID artistId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Earning e WHERE e.artist.id = :artistId " +
           "AND e.status IN ('AVAILABLE', 'PAID_OUT') " +
           "AND e.createdAt >= :startDate AND e.createdAt < :endDate")
    BigDecimal sumEarningsForPeriod(
            @Param("artistId") UUID artistId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Earning e WHERE e.artist.id = :artistId AND e.status = 'AVAILABLE'")
    BigDecimal sumPendingPayout(@Param("artistId") UUID artistId);

    boolean existsByOrderId(UUID orderId);

    long countByArtistId(UUID artistId);
}


