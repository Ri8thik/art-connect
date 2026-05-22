package com.project.artconnect.modules.reviews.service;

import com.project.artconnect.common.enums.OrderStatus;
import com.project.artconnect.modules.artists.repository.ArtistProfileRepository;
import com.project.artconnect.modules.orders.entity.Order;
import com.project.artconnect.modules.orders.repository.OrderRepository;
import com.project.artconnect.modules.reviews.dto.CreateReviewRequest;
import com.project.artconnect.modules.reviews.dto.ReviewDto;
import com.project.artconnect.modules.reviews.entity.Review;
import com.project.artconnect.modules.reviews.repository.ReviewRepository;
import com.project.artconnect.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ArtistProfileRepository artistProfileRepository;

    @Transactional
    public ReviewDto createReview(User customer, CreateReviewRequest request) {
        // If orderId is provided, validate the order and customer permission
        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found: " + request.getOrderId()));

            if (!order.getCustomer().getId().equals(customer.getId())) {
                throw new RuntimeException("You can only review orders you placed");
            }

            if (order.getStatus() != OrderStatus.DELIVERED) {
                throw new RuntimeException("You can only review a delivered order");
            }

            if (reviewRepository.existsByOrderId(order.getId())) {
                throw new RuntimeException("A review already exists for this order");
            }
        }

        // Get artist from request or order
        User artist = null;
        if (request.getArtistId() != null) {
            artist = artistProfileRepository.findByUserId(request.getArtistId())
                    .map(profile -> profile.getUser())
                    .orElseThrow(() -> new RuntimeException("Artist not found: " + request.getArtistId()));
        } else if (order != null) {
            artist = order.getArtist();
        } else {
            throw new RuntimeException("Either orderId or artistId must be provided");
        }

        Review review = Review.builder()
                .order(order)
                .artist(artist)
                .customer(customer)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        review = reviewRepository.save(review);

        // Update artist rating summary
        updateArtistRatingSummary(artist.getId());

        return mapToDto(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> getArtistReviews(UUID artistId, Pageable pageable) {
        return reviewRepository.findByArtistIdOrderByCreatedAtDesc(artistId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public ReviewDto getOrderReview(UUID orderId) {
        Review review = reviewRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No review found for order: " + orderId));
        return mapToDto(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> getMyReviews(User customer, Pageable pageable) {
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(customer.getId(), pageable).map(this::mapToDto);
    }

    private void updateArtistRatingSummary(UUID artistId) {
        artistProfileRepository.findByUserId(artistId).ifPresent(profile -> {
            Double avg = reviewRepository.findAverageRatingByArtistId(artistId);
            Long count = reviewRepository.findReviewCountByArtistId(artistId);
            profile.setRatingAvg(avg != null ? avg : 0.0);
            profile.setRatingCount(count != null ? count.intValue() : 0);
            artistProfileRepository.save(profile);
        });
    }

    private ReviewDto mapToDto(Review review) {
        UUID orderId = review.getOrder() != null ? review.getOrder().getId() : null;
        String orderTitle = review.getOrder() != null ? review.getOrder().getTitle() : null;

        return ReviewDto.builder()
                .id(review.getId())
                .orderId(orderId)
                .orderTitle(orderTitle)
                .artistId(review.getArtist().getId())
                .artistName(review.getArtist().getDisplayName())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getDisplayName())
                .customerPhotoUrl(review.getCustomer().getPhotoUrl())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}

