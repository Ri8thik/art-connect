package com.project.artconnect.modules.reviews.controller;

import com.project.artconnect.common.response.ApiResponse;
import com.project.artconnect.modules.reviews.dto.CreateReviewRequest;
import com.project.artconnect.modules.reviews.dto.ReviewDto;
import com.project.artconnect.modules.reviews.service.ReviewService;
import com.project.artconnect.modules.users.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Customer reviews and ratings")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Submit a review for a delivered order")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted", reviewService.createReview(user, request)));
    }

    @GetMapping("/artist/{artistId}")
    @Operation(summary = "Get all reviews for an artist")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getArtistReviews(
            @PathVariable UUID artistId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewDto> reviews = reviewService.getArtistReviews(artistId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get review for a specific order")
    public ResponseEntity<ApiResponse<ReviewDto>> getOrderReview(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getOrderReview(orderId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get reviews submitted by current user")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getMyReviews(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReviewDto> reviews = reviewService.getMyReviews(user,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }
}

