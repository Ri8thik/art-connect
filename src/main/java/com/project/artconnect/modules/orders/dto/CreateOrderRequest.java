package com.project.artconnect.modules.orders.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Artist ID is required")
    private UUID artistId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private String category;

    private List<String> referenceImageUrls;

    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String budgetCurrency;

    private LocalDateTime deadline;
}

