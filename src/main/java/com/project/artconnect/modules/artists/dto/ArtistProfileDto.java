package com.project.artconnect.modules.artists.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ArtistProfileDto {
    private UUID id;
    private String displayName;
    private String email;
    private String photoUrl;
    private String locationCity;
    private String locationState;
    private String locationCountry;
    private String bio;
    private List<String> skills;
    private List<String> categories;
    private BigDecimal startingPrice;
    private String pricingCurrency;
    private String pricingNotes;
    private String availability;
    private Double ratingAvg;
    private Integer ratingCount;
    private boolean isActive;
    private List<PortfolioItemDto> portfolio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    public static class PortfolioItemDto {
        private UUID id;
        private String imageUrl;
        private String title;
        private String description;
        private String category;
        private LocalDateTime createdAt;
    }
}

