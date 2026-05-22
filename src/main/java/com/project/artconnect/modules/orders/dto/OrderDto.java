package com.project.artconnect.modules.orders.dto;

import com.project.artconnect.common.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderDto {
    private UUID id;
    private UUID customerId;
    private String customerName;
    private String customerPhotoUrl;
    private UUID artistId;
    private String artistName;
    private String artistPhotoUrl;
    private OrderStatus status;
    private String title;
    private String description;
    private String category;
    private List<String> referenceImageUrls;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String budgetCurrency;
    private LocalDateTime deadline;
    private BigDecimal agreedPrice;
    private BigDecimal platformFee;
    private BigDecimal artistNet;
    private String pricingCurrency;
    private List<StatusHistoryDto> statusHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class StatusHistoryDto {
        private OrderStatus status;
        private String changedBy;
        private LocalDateTime changedAt;
    }
}

