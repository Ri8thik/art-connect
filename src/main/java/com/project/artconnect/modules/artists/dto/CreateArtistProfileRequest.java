package com.project.artconnect.modules.artists.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateArtistProfileRequest {

    @NotBlank(message = "Bio is required")
    @Size(max = 2000)
    private String bio;

    private List<String> skills;

    private List<String> categories;

    private BigDecimal startingPrice;
    private String pricingCurrency;
    private String pricingNotes;
    private String availability;

    private List<PortfolioItemInput> portfolio;

    @Data
    public static class PortfolioItemInput {
        @Size(max = 500, message = "Title must not exceed 500 characters")
        private String title;

        @Size(max = 65535, message = "Image URL must not exceed 65535 characters")
        private String imageUrl;

        @Size(max = 500, message = "Category must not exceed 500 characters")
        private String category;

        @Size(max = 65535, message = "Description must not exceed 65535 characters")
        private String description;
    }
}

