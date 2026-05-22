package com.project.artconnect.modules.reviews.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewDto {
    private UUID id;
    private UUID orderId;
    private String orderTitle;
    private UUID artistId;
    private String artistName;
    private UUID customerId;
    private String customerName;
    private String customerPhotoUrl;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}

