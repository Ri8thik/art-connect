package com.project.artconnect.modules.payouts.service;

import com.project.artconnect.common.enums.EarningStatus;
import com.project.artconnect.common.enums.PayoutStatus;
import com.project.artconnect.modules.earnings.entity.Earning;
import com.project.artconnect.modules.earnings.repository.EarningRepository;
import com.project.artconnect.modules.payouts.dto.PayoutDto;
import com.project.artconnect.modules.payouts.entity.Payout;
import com.project.artconnect.modules.payouts.repository.PayoutRepository;
import com.project.artconnect.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PayoutRepository payoutRepository;
    private final EarningRepository earningRepository;

    @Transactional(readOnly = true)
    public Page<PayoutDto> getPayouts(User artist, Pageable pageable) {
        return payoutRepository.findByArtistIdOrderByCreatedAtDesc(artist.getId(), pageable)
                .map(this::mapToDto);
    }

    /**
     * Request a payout for all AVAILABLE earnings.
     * In production, this would trigger a payment provider API.
     */
    @Transactional
    public PayoutDto requestPayout(User artist) {
        BigDecimal pendingAmount = earningRepository.sumPendingPayout(artist.getId());

        if (pendingAmount == null || pendingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("No available earnings to pay out");
        }

        LocalDateTime now = LocalDateTime.now();

        Payout payout = Payout.builder()
                .artist(artist)
                .periodStart(now.minusMonths(1))
                .periodEnd(now)
                .grossAmount(pendingAmount)
                .netAmount(pendingAmount)   // simplified; real impl would subtract fees
                .currency("USD")
                .status(PayoutStatus.PROCESSING)
                .build();

        payout = payoutRepository.save(payout);

        // Mark all AVAILABLE earnings as PAID_OUT
        // (In production, do this only after payment provider confirms)
        earningRepository.findByArtistIdOrderByCreatedAtDesc(artist.getId(), PageRequest.of(0, Integer.MAX_VALUE))
                .stream()
                .filter(e -> e.getStatus() == EarningStatus.AVAILABLE)
                .forEach(e -> {
                    e.setStatus(EarningStatus.PAID_OUT);
                    earningRepository.save(e);
                });

        return mapToDto(payout);
    }

    private PayoutDto mapToDto(Payout payout) {
        return PayoutDto.builder()
                .id(payout.getId())
                .artistId(payout.getArtist().getId())
                .periodStart(payout.getPeriodStart())
                .periodEnd(payout.getPeriodEnd())
                .grossAmount(payout.getGrossAmount())
                .netAmount(payout.getNetAmount())
                .currency(payout.getCurrency())
                .status(payout.getStatus())
                .providerRef(payout.getProviderRef())
                .createdAt(payout.getCreatedAt())
                .build();
    }
}

