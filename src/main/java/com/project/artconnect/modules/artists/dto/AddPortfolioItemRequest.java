package com.project.artconnect.modules.artists.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddPortfolioItemRequest {

    @Size(max = 65535, message = "Image URL must not exceed 65535 characters")
    private String imageUrl;

    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;

    @Size(max = 65535, message = "Description must not exceed 65535 characters")
    private String description;

    @Size(max = 500, message = "Category must not exceed 500 characters")
    private String category;
}

