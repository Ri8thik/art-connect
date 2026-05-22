package com.project.artconnect.modules.earnings.dto;

import com.project.artconnect.common.enums.EarningStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EarningDto {
    private UUID id;
    private UUID artistId;
    private UUID orderId;
    private String orderTitle;
    private BigDecimal amount;
    private String currency;
    private EarningStatus status;
    private LocalDateTime createdAt;
}

