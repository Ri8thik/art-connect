package com.project.artconnect.modules.orders.dto;

import com.project.artconnect.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    // Optional pricing info when accepting an order
    private BigDecimal agreedPrice;
    private BigDecimal platformFee;
    private String pricingCurrency;
}

