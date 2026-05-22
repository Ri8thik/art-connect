package com.project.artconnect.modules.admin.service;

import com.project.artconnect.common.enums.*;
import com.project.artconnect.modules.admin.dto.AdminDtos;
import com.project.artconnect.modules.admin.entity.AdminAuditLog;
import com.project.artconnect.modules.admin.repository.AdminAuditLogRepository;
import com.project.artconnect.modules.artists.entity.ArtistProfile;
import com.project.artconnect.modules.artists.repository.ArtistProfileRepository;
import com.project.artconnect.modules.earnings.repository.EarningRepository;
import com.project.artconnect.modules.notifications.entity.Notification;
import com.project.artconnect.modules.notifications.repository.NotificationRepository;
import com.project.artconnect.modules.orders.entity.Order;
import com.project.artconnect.modules.orders.entity.StatusHistoryEntry;
import com.project.artconnect.modules.orders.repository.OrderRepository;
import com.project.artconnect.modules.payouts.entity.Payout;
import com.project.artconnect.modules.payouts.repository.PayoutRepository;
import com.project.artconnect.modules.reviews.repository.ReviewRepository;
import com.project.artconnect.modules.users.entity.User;
import com.project.artconnect.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final PayoutRepository payoutRepository;
    private final EarningRepository earningRepository;
    private final NotificationRepository notificationRepository;
    private final AdminAuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── USERS ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminDtos.AdminUserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapUserToDto);
    }

    @Transactional(readOnly = true)
    public AdminDtos.AdminUserDto getUserById(UUID id) {
        return mapUserToDto(findUserOrThrow(id));
    }

    @Transactional
    public AdminDtos.AdminUserDto updateUser(UUID id, AdminDtos.UpdateUserRequest req, User admin) {
        User user = findUserOrThrow(id);
        if (req.getDisplayName() != null) user.setDisplayName(req.getDisplayName());
        if (req.getEmail() != null) user.setEmail(req.getEmail());
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getLocationCity() != null) user.setLocationCity(req.getLocationCity());
        if (req.getLocationState() != null) user.setLocationState(req.getLocationState());
        if (req.getLocationCountry() != null) user.setLocationCountry(req.getLocationCountry());
        if (req.getRoles() != null && !req.getRoles().isEmpty()) {
            user.setRoles(new HashSet<>(req.getRoles()));
        }
        userRepository.save(user);
        audit(admin, "UPDATE_USER", "USER", id.toString(), "Updated user profile");
        return mapUserToDto(user);
    }

    @Transactional
    public AdminDtos.AdminUserDto createUser(AdminDtos.CreateUserRequest req, User admin) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already in use: " + req.getEmail());
        }
        User user = User.builder()
                .displayName(req.getDisplayName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .locationCity(req.getLocationCity())
                .locationState(req.getLocationState())
                .locationCountry(req.getLocationCountry())
                .roles(req.getRoles() != null ? new HashSet<>(req.getRoles()) : new HashSet<>(List.of(UserRole.CUSTOMER)))
                .build();
        userRepository.save(user);
        audit(admin, "CREATE_USER", "USER", user.getId().toString(), "Created user: " + user.getEmail());
        return mapUserToDto(user);
    }

    @Transactional
    public void deleteUser(UUID id, User admin) {
        User user = findUserOrThrow(id);
        String email = user.getEmail();
        userRepository.delete(user);
        audit(admin, "DELETE_USER", "USER", id.toString(), "Deleted user: " + email);
    }

    @Transactional
    public AdminDtos.AdminUserDto updateUserStatus(UUID id, AdminDtos.UserStatusRequest req, User admin) {
        User user = findUserOrThrow(id);
        user.setStatus(req.getStatus());
        user.setSuspendReason(req.getReason());
        if (req.getStatus() == UserStatus.SUSPENDED && req.getSuspendDays() != null && req.getSuspendDays() > 0) {
            user.setSuspendedUntil(LocalDateTime.now().plusDays(req.getSuspendDays()));
        } else {
            user.setSuspendedUntil(null);
        }
        if (req.getStatus() == UserStatus.ACTIVE) {
            user.setSuspendReason(null);
            user.setSuspendedUntil(null);
        }
        userRepository.save(user);
        audit(admin, "UPDATE_USER_STATUS", "USER", id.toString(),
                "Status set to " + req.getStatus() + (req.getReason() != null ? ": " + req.getReason() : ""));
        return mapUserToDto(user);
    }

    // ─── ARTISTS ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminDtos.AdminArtistDto> getAllArtists(Pageable pageable) {
        return artistProfileRepository.findAll(pageable).map(this::mapArtistToDto);
    }

    @Transactional(readOnly = true)
    public AdminDtos.AdminArtistDto getArtistById(UUID id) {
        ArtistProfile profile = artistProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist profile not found: " + id));
        return mapArtistToDto(profile);
    }

    @Transactional
    public AdminDtos.AdminArtistDto approveArtist(UUID id, AdminDtos.ApproveArtistRequest req, User admin) {
        ArtistProfile profile = artistProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found: " + id));
        profile.setApprovalStatus(req.isApproved() ? ApprovalStatus.APPROVED : ApprovalStatus.REJECTED);
        profile.setApprovalReason(req.getReason());
        if (!req.isApproved()) {
            profile.setActive(false);
        }
        artistProfileRepository.save(profile);

        // Notify artist
        String msg = req.isApproved()
                ? "Your artist profile has been approved! You're now visible to customers."
                : "Your artist profile was not approved. Reason: " + (req.getReason() != null ? req.getReason() : "Not specified");
        sendNotificationToUser(profile.getUser(), NotificationType.GENERAL,
                req.isApproved() ? "Profile Approved ✅" : "Profile Not Approved",
                msg, null, id.toString());

        audit(admin, req.isApproved() ? "APPROVE_ARTIST" : "REJECT_ARTIST", "ARTIST", id.toString(),
                req.getReason() != null ? req.getReason() : "");
        return mapArtistToDto(profile);
    }

    @Transactional
    public AdminDtos.AdminArtistDto featureArtist(UUID id, AdminDtos.FeatureArtistRequest req, User admin) {
        ArtistProfile profile = artistProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artist not found: " + id));
        profile.setFeatured(req.isFeatured());
        profile.setFeaturedRank(req.getRank());
        artistProfileRepository.save(profile);
        audit(admin, "FEATURE_ARTIST", "ARTIST", id.toString(),
                "Featured=" + req.isFeatured() + ", rank=" + req.getRank());
        return mapArtistToDto(profile);
    }

    @Transactional
    public void deleteArtistPortfolioItem(UUID artistId, UUID itemId, User admin) {
        ArtistProfile profile = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist not found: " + artistId));
        profile.getPortfolio().removeIf(item -> item.getId().equals(itemId));
        artistProfileRepository.save(profile);
        audit(admin, "DELETE_PORTFOLIO_ITEM", "ARTIST", artistId.toString(), "Removed portfolio item: " + itemId);
    }

    // ─── ORDERS ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminDtos.AdminOrderDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapOrderToDto);
    }

    @Transactional(readOnly = true)
    public AdminDtos.AdminOrderDto getOrderById(UUID id) {
        return mapOrderToDto(findOrderOrThrow(id));
    }

    @Transactional
    public AdminDtos.AdminOrderDto overrideOrderStatus(UUID id, AdminDtos.AdminOrderStatusRequest req, User admin) {
        Order order = findOrderOrThrow(id);
        OrderStatus newStatus = OrderStatus.valueOf(req.getStatus().toUpperCase());
        order.setStatus(newStatus);
        StatusHistoryEntry entry = StatusHistoryEntry.builder()
                .order(order)
                .status(newStatus)
                .changedBy("ADMIN:" + admin.getEmail() + (req.getReason() != null ? " - " + req.getReason() : ""))
                .build();
        order.getStatusHistory().add(entry);
        orderRepository.save(order);
        audit(admin, "OVERRIDE_ORDER_STATUS", "ORDER", id.toString(),
                "Status set to " + newStatus + ": " + req.getReason());
        return mapOrderToDto(order);
    }

    @Transactional
    public void deleteOrder(UUID id, User admin) {
        Order order = findOrderOrThrow(id);
        orderRepository.delete(order);
        audit(admin, "DELETE_ORDER", "ORDER", id.toString(), "Deleted order: " + order.getTitle());
    }

    // ─── REVIEWS ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminDtos.AdminReviewDto> getAllReviews(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(this::mapReviewToDto);
    }

    @Transactional
    public void deleteReview(UUID id, String reason, User admin) {
        var review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found: " + id));
        UUID artistId = review.getArtist().getId();
        reviewRepository.delete(review);
        // Recalculate artist rating
        Double newAvg = reviewRepository.findAverageRatingByArtistId(artistId);
        Long newCount = reviewRepository.findReviewCountByArtistId(artistId);
        artistProfileRepository.findById(artistId).ifPresent(p -> {
            p.setRatingAvg(newAvg != null ? newAvg : 0.0);
            p.setRatingCount(newCount != null ? newCount.intValue() : 0);
            artistProfileRepository.save(p);
        });
        audit(admin, "DELETE_REVIEW", "REVIEW", id.toString(), reason != null ? reason : "Removed by admin");
    }

    // ─── PAYOUTS ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminDtos.AdminPayoutDto> getAllPayouts(Pageable pageable) {
        return payoutRepository.findAll(pageable).map(this::mapPayoutToDto);
    }

    @Transactional
    public AdminDtos.AdminPayoutDto approvePayout(UUID id, AdminDtos.PayoutActionRequest req, User admin) {
        Payout payout = findPayoutOrThrow(id);
        payout.setStatus(PayoutStatus.PAID);
        payout.setProviderRef(req.getTransactionReference());
        payoutRepository.save(payout);
        sendNotificationToUser(payout.getArtist(), NotificationType.GENERAL,
                "Payout Processed 💸",
                "Your payout of " + payout.getNetAmount() + " " + payout.getCurrency() + " has been processed!",
                null, payout.getArtist().getId().toString());
        audit(admin, "APPROVE_PAYOUT", "PAYOUT", id.toString(), "Ref: " + req.getTransactionReference());
        return mapPayoutToDto(payout);
    }

    @Transactional
    public AdminDtos.AdminPayoutDto rejectPayout(UUID id, AdminDtos.PayoutActionRequest req, User admin) {
        Payout payout = findPayoutOrThrow(id);
        payout.setStatus(PayoutStatus.FAILED);
        payoutRepository.save(payout);
        sendNotificationToUser(payout.getArtist(), NotificationType.GENERAL,
                "Payout Rejected",
                "Your payout request was rejected. Reason: " + (req.getReason() != null ? req.getReason() : "Not specified"),
                null, payout.getArtist().getId().toString());
        audit(admin, "REJECT_PAYOUT", "PAYOUT", id.toString(), req.getReason());
        return mapPayoutToDto(payout);
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────

    @Transactional
    public int broadcastNotification(AdminDtos.BroadcastNotificationRequest req, User admin) {
        List<User> targets = resolveTargets(req.getTarget());
        NotificationType type = parseNotificationType(req.getType());
        for (User u : targets) {
            sendNotificationToUser(u, type, req.getTitle(), req.getBody(), null, null);
        }
        audit(admin, "BROADCAST_NOTIFICATION", "NOTIFICATIONS", req.getTarget(),
                "Sent \"" + req.getTitle() + "\" to " + targets.size() + " users");
        return targets.size();
    }

    // ─── ANALYTICS ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminDtos.PlatformAnalyticsDto getAnalytics() {
        long totalUsers = userRepository.count();
        long totalArtists = artistProfileRepository.count();
        long totalOrders = orderRepository.count();

        // For revenue estimates: sum of all earnings amount
        double totalRevenue = earningRepository.findAll().stream()
                .mapToDouble(e -> e.getAmount().doubleValue()).sum();
        double platformFee = totalRevenue * 0.05;

        // Month range
        YearMonth thisMonth = YearMonth.now();
        LocalDateTime monthStart = thisMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = thisMonth.atEndOfMonth().atTime(23, 59, 59);

        long newUsersThisMonth = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(monthStart)).count();
        long ordersThisMonth = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(monthStart)).count();
        double revenueThisMonth = earningRepository.findAll().stream()
                .filter(e -> e.getCreatedAt() != null && e.getCreatedAt().isAfter(monthStart))
                .mapToDouble(e -> e.getAmount().doubleValue()).sum();

        long completedOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();

        // Avg order value
        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // Avg artist rating
        double avgArtistRating = artistProfileRepository.findAll().stream()
                .filter(a -> a.getRatingAvg() != null && a.getRatingAvg() > 0)
                .mapToDouble(ArtistProfile::getRatingAvg).average().orElse(0.0);

        return AdminDtos.PlatformAnalyticsDto.builder()
                .totalUsers(totalUsers)
                .totalArtists(totalArtists)
                .totalOrders(totalOrders)
                .completedOrders(completedOrders)
                .pendingOrders(totalOrders - completedOrders)
                .totalRevenue(totalRevenue)
                .platformFeeCollected(platformFee)
                .pendingPayouts(0)
                .avgOrderValue(avgOrderValue)
                .avgArtistRating(avgArtistRating)
                .newUsersThisMonth(newUsersThisMonth)
                .ordersThisMonth(ordersThisMonth)
                .revenueThisMonth(revenueThisMonth)
                .build();
    }

    // ─── AUDIT LOG ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AdminDtos.AuditLogDto> getAuditLog(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::mapAuditToDto);
    }

    // ─── HELPERS ─────────────────────────────────────────────

    private void audit(User admin, String action, String targetType, String targetId, String details) {
        auditLogRepository.save(AdminAuditLog.builder()
                .adminUser(admin)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build());
    }

    private void sendNotificationToUser(User user, NotificationType type, String title, String body,
                                         String orderId, String artistId) {
        Notification n = Notification.builder()
                .recipient(user)
                .type(type)
                .title(title)
                .body(body)
                .relatedOrderId(orderId)
                .relatedArtistId(artistId)
                .read(false)
                .build();
        notificationRepository.save(n);
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    private Order findOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    private Payout findPayoutOrThrow(UUID id) {
        return payoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payout not found: " + id));
    }

    private List<User> resolveTargets(String target) {
        if (target == null || target.equalsIgnoreCase("ALL")) {
            return userRepository.findAll();
        }
        if (target.equalsIgnoreCase("ALL_ARTISTS")) {
            return userRepository.findAll().stream()
                    .filter(u -> u.getRoles().contains(UserRole.ARTIST)).toList();
        }
        if (target.equalsIgnoreCase("ALL_CUSTOMERS")) {
            return userRepository.findAll().stream()
                    .filter(u -> u.getRoles().contains(UserRole.CUSTOMER)).toList();
        }
        if (target.toUpperCase().startsWith("USER:")) {
            String idStr = target.substring(5);
            try {
                return List.of(findUserOrThrow(UUID.fromString(idStr)));
            } catch (Exception e) {
                return List.of();
            }
        }
        return userRepository.findAll();
    }

    private NotificationType parseNotificationType(String type) {
        if (type == null) return NotificationType.GENERAL;
        try {
            return NotificationType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            return NotificationType.GENERAL;
        }
    }

    // ─── MAPPERS ─────────────────────────────────────────────

    private AdminDtos.AdminUserDto mapUserToDto(User u) {
        long orderCount = orderRepository.findByCustomerId(u.getId(), PageRequest.of(0, 1)).getTotalElements()
                + orderRepository.findByArtistId(u.getId(), PageRequest.of(0, 1)).getTotalElements();
        return AdminDtos.AdminUserDto.builder()
                .id(u.getId())
                .displayName(u.getDisplayName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .photoUrl(u.getPhotoUrl())
                .roles(u.getRoles())
                .status(u.getStatus() != null ? u.getStatus() : UserStatus.ACTIVE)
                .locationCity(u.getLocationCity())
                .locationState(u.getLocationState())
                .locationCountry(u.getLocationCountry())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .suspendReason(u.getSuspendReason())
                .suspendedUntil(u.getSuspendedUntil())
                .orderCount(orderCount)
                .hasArtistProfile(artistProfileRepository.existsByUserId(u.getId()))
                .build();
    }

    private AdminDtos.AdminArtistDto mapArtistToDto(ArtistProfile p) {
        long orderCount = orderRepository.findByArtistId(p.getId(), PageRequest.of(0, 1)).getTotalElements();
        return AdminDtos.AdminArtistDto.builder()
                .id(p.getId())
                .displayName(p.getUser().getDisplayName())
                .email(p.getUser().getEmail())
                .photoUrl(p.getUser().getPhotoUrl())
                .bio(p.getBio())
                .skills(new ArrayList<>(p.getSkills()))
                .categories(new ArrayList<>(p.getCategories()))
                .startingPrice(p.getStartingPrice())
                .availability(p.getAvailability())
                .ratingAvg(p.getRatingAvg())
                .ratingCount(p.getRatingCount())
                .isActive(p.isActive())
                .approvalStatus(p.getApprovalStatus() != null ? p.getApprovalStatus() : ApprovalStatus.APPROVED)
                .approvalReason(p.getApprovalReason())
                .featured(p.isFeatured())
                .featuredRank(p.getFeaturedRank())
                .locationCity(p.getUser().getLocationCity())
                .locationState(p.getUser().getLocationState())
                .locationCountry(p.getUser().getLocationCountry())
                .createdAt(p.getCreatedAt())
                .portfolioCount(p.getPortfolio() != null ? p.getPortfolio().size() : 0)
                .orderCount(orderCount)
                .build();
    }

    private AdminDtos.AdminOrderDto mapOrderToDto(Order o) {
        return AdminDtos.AdminOrderDto.builder()
                .id(o.getId())
                .customerName(o.getCustomer().getDisplayName())
                .customerEmail(o.getCustomer().getEmail())
                .customerId(o.getCustomer().getId())
                .artistName(o.getArtist().getDisplayName())
                .artistEmail(o.getArtist().getEmail())
                .artistId(o.getArtist().getId())
                .status(o.getStatus().name())
                .title(o.getTitle())
                .description(o.getDescription())
                .category(o.getCategory())
                .budgetMin(o.getBudgetMin())
                .budgetMax(o.getBudgetMax())
                .agreedPrice(o.getAgreedPrice())
                .platformFee(o.getPlatformFee())
                .artistNet(o.getArtistNet())
                .deadline(o.getDeadline())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .referenceImageUrls(new ArrayList<>(o.getReferenceImageUrls()))
                .build();
    }

    private AdminDtos.AdminReviewDto mapReviewToDto(com.project.artconnect.modules.reviews.entity.Review r) {
        return AdminDtos.AdminReviewDto.builder()
                .id(r.getId())
                .reviewerName(r.getCustomer().getDisplayName())
                .reviewerEmail(r.getCustomer().getEmail())
                .reviewerId(r.getCustomer().getId())
                .artistName(r.getArtist().getDisplayName())
                .artistId(r.getArtist().getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .orderId(r.getOrder() != null ? r.getOrder().getId() : null)
                .createdAt(r.getCreatedAt())
                .build();
    }

    private AdminDtos.AdminPayoutDto mapPayoutToDto(Payout p) {
        return AdminDtos.AdminPayoutDto.builder()
                .id(p.getId())
                .artistName(p.getArtist().getDisplayName())
                .artistEmail(p.getArtist().getEmail())
                .artistId(p.getArtist().getId())
                .amount(p.getNetAmount())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .transactionReference(p.getProviderRef())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private AdminDtos.AuditLogDto mapAuditToDto(AdminAuditLog log) {
        return AdminDtos.AuditLogDto.builder()
                .id(log.getId())
                .adminName(log.getAdminUser().getDisplayName())
                .adminEmail(log.getAdminUser().getEmail())
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}


