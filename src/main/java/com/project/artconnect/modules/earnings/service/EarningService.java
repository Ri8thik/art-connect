package com.project.artconnect.modules.earnings.service;

import com.project.artconnect.common.enums.EarningStatus;
import com.project.artconnect.modules.earnings.dto.EarningDto;
import com.project.artconnect.modules.earnings.dto.EarningsSummaryDto;
import com.project.artconnect.modules.earnings.entity.Earning;
import com.project.artconnect.modules.earnings.repository.EarningRepository;
import com.project.artconnect.modules.orders.entity.Order;
import com.project.artconnect.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EarningService {

    private final EarningRepository earningRepository;

    @Transactional(readOnly = true)
    public Page<EarningDto> getEarnings(User artist, Pageable pageable) {
        return earningRepository.findByArtistIdOrderByCreatedAtDesc(artist.getId(), pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public EarningsSummaryDto getEarningsSummary(User artist) {
        UUID artistId = artist.getId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime monthEnd = now.with(TemporalAdjusters.firstDayOfNextMonth()).toLocalDate().atStartOfDay();

        BigDecimal thisMonth = earningRepository.sumEarningsForPeriod(artistId, monthStart, monthEnd);
        BigDecimal pendingPayout = earningRepository.sumPendingPayout(artistId);
        BigDecimal allTime = earningRepository.sumEarningsForPeriod(artistId,
                LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.of(2100, 1, 1, 0, 0));

        return EarningsSummaryDto.builder()
                .thisMonthTotal(thisMonth != null ? thisMonth : BigDecimal.ZERO)
                .pendingPayout(pendingPayout != null ? pendingPayout : BigDecimal.ZERO)
                .allTimeTotal(allTime != null ? allTime : BigDecimal.ZERO)
                .totalOrders(earningRepository.countByArtistId(artistId))
                .build();
    }

    /**
     * Called internally when an order is delivered.
     */
    @Transactional
    public void createEarningForOrder(Order order) {
        if (earningRepository.existsByOrderId(order.getId())) {
            return; // idempotent
        }

        BigDecimal amount = order.getArtistNet() != null
                ? order.getArtistNet()
                : (order.getAgreedPrice() != null ? order.getAgreedPrice() : BigDecimal.ZERO);

        Earning earning = Earning.builder()
                .artist(order.getArtist())
                .order(order)
                .amount(amount)
                .currency(order.getPricingCurrency() != null ? order.getPricingCurrency() : "USD")
                .status(EarningStatus.AVAILABLE)
                .build();

        earningRepository.save(earning);
    }

    private EarningDto mapToDto(Earning earning) {
        return EarningDto.builder()
                .id(earning.getId())
                .artistId(earning.getArtist().getId())
                .orderId(earning.getOrder().getId())
                .orderTitle(earning.getOrder().getTitle())
                .amount(earning.getAmount())
                .currency(earning.getCurrency())
                .status(earning.getStatus())
                .createdAt(earning.getCreatedAt())
                .build();
    }
}

