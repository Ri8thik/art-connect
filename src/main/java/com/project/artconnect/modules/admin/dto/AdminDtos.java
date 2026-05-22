package com.project.artconnect.modules.admin.dto;

import com.project.artconnect.common.enums.ApprovalStatus;
import com.project.artconnect.common.enums.UserRole;
import com.project.artconnect.common.enums.UserStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdminDtos {

    /* ─── Users ─── */

    @Data @Builder
    public static class AdminUserDto {
        private UUID id;
        private String displayName;
        private String email;
        private String phone;
        private String photoUrl;
        private Set<UserRole> roles;
        private UserStatus status;
        private String locationCity;
        private String locationState;
        private String locationCountry;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String suspendReason;
        private LocalDateTime suspendedUntil;
        private long orderCount;
        private boolean hasArtistProfile;
    }

    @Data
    public static class UpdateUserRequest {
        private String displayName;
        private String email;
        private String phone;
        private String locationCity;
        private String locationState;
        private String locationCountry;
        private Set<UserRole> roles;
    }

    @Data
    public static class UserStatusRequest {
        private UserStatus status;
        private Integer suspendDays;
        private String reason;
    }

    @Data
    public static class CreateUserRequest {
        private String displayName;
        private String email;
        private String password;
        private Set<UserRole> roles;
        private String locationCity;
        private String locationState;
        private String locationCountry;
    }

    /* ─── Artists ─── */

    @Data @Builder
    public static class AdminArtistDto {
        private UUID id;
        private String displayName;
        private String email;
        private String photoUrl;
        private String bio;
        private List<String> skills;
        private List<String> categories;
        private BigDecimal startingPrice;
        private String availability;
        private Double ratingAvg;
        private Integer ratingCount;
        private boolean isActive;
        private ApprovalStatus approvalStatus;
        private String approvalReason;
        private boolean featured;
        private Integer featuredRank;
        private String locationCity;
        private String locationState;
        private String locationCountry;
        private LocalDateTime createdAt;
        private int portfolioCount;
        private long orderCount;
    }

    @Data
    public static class ApproveArtistRequest {
        private boolean approved;
        private String reason;
    }

    @Data
    public static class FeatureArtistRequest {
        private boolean featured;
        private Integer rank;
    }

    /* ─── Orders ─── */

    @Data @Builder
    public static class AdminOrderDto {
        private UUID id;
        private String customerName;
        private String customerEmail;
        private UUID customerId;
        private String artistName;
        private String artistEmail;
        private UUID artistId;
        private String status;
        private String title;
        private String description;
        private String category;
        private BigDecimal budgetMin;
        private BigDecimal budgetMax;
        private BigDecimal agreedPrice;
        private BigDecimal platformFee;
        private BigDecimal artistNet;
        private LocalDateTime deadline;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<String> referenceImageUrls;
    }

    @Data
    public static class AdminOrderStatusRequest {
        private String status;
        private String reason;
    }

    /* ─── Reviews ─── */

    @Data @Builder
    public static class AdminReviewDto {
        private UUID id;
        private String reviewerName;
        private String reviewerEmail;
        private UUID reviewerId;
        private String artistName;
        private UUID artistId;
        private int rating;
        private String comment;
        private UUID orderId;
        private LocalDateTime createdAt;
    }

    /* ─── Payouts ─── */

    @Data @Builder
    public static class AdminPayoutDto {
        private UUID id;
        private String artistName;
        private String artistEmail;
        private UUID artistId;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String paymentMethod;
        private String transactionReference;
        private String rejectionReason;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class PayoutActionRequest {
        private String transactionReference;
        private String reason;
    }

    /* ─── Notifications ─── */

    @Data
    public static class BroadcastNotificationRequest {
        private String target;   // ALL, ALL_ARTISTS, ALL_CUSTOMERS, USER:{id}
        private String type;
        private String title;
        private String body;
    }

    /* ─── Analytics ─── */

    @Data @Builder
    public static class PlatformAnalyticsDto {
        private long totalUsers;
        private long totalArtists;
        private long totalOrders;
        private long completedOrders;
        private long pendingOrders;
        private double totalRevenue;
        private double platformFeeCollected;
        private double pendingPayouts;
        private double avgOrderValue;
        private double avgArtistRating;
        private long newUsersThisMonth;
        private long ordersThisMonth;
        private double revenueThisMonth;
    }

    /* ─── Audit Log ─── */

    @Data @Builder
    public static class AuditLogDto {
        private UUID id;
        private String adminName;
        private String adminEmail;
        private String action;
        private String targetType;
        private String targetId;
        private String details;
        private LocalDateTime createdAt;
    }
}

