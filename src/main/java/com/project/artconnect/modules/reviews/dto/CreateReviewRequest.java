package com.project.artconnect.modules.reviews.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReviewRequest {

    private UUID orderId;  // Optional - review can be standalone

    @NotNull(message = "Artist ID is required")
    private UUID artistId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 2000)
    private String comment;
}

