package com.project.artconnect.modules.earnings.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class EarningsSummaryDto {
    private BigDecimal thisMonthTotal;
    private BigDecimal pendingPayout;
    private BigDecimal allTimeTotal;
    private long totalOrders;
}

