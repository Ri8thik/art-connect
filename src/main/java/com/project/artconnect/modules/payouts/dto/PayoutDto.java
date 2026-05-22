package com.project.artconnect.modules.payouts.dto;

import com.project.artconnect.common.enums.PayoutStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PayoutDto {
    private UUID id;
    private UUID artistId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private String currency;
    private PayoutStatus status;
    private String providerRef;
    private LocalDateTime createdAt;
}

