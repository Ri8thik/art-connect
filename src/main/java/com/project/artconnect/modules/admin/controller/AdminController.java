package com.project.artconnect.modules.admin.controller;

import com.project.artconnect.common.response.ApiResponse;
import com.project.artconnect.modules.admin.dto.AdminDtos;
import com.project.artconnect.modules.admin.service.AdminService;
import com.project.artconnect.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ─── USERS ───────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<AdminDtos.AdminUserDto>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminDtos.AdminUserDto> users = adminService.getAllUsers(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminDtos.AdminUserDto>> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getUserById(id)));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<AdminDtos.AdminUserDto>> createUser(
            @RequestBody AdminDtos.CreateUserRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.createUser(req, admin)));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<AdminDtos.AdminUserDto>> updateUser(
            @PathVariable UUID id,
            @RequestBody AdminDtos.UpdateUserRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUser(id, req, admin)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {
        adminService.deleteUser(id, admin);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<AdminDtos.AdminUserDto>> updateUserStatus(
            @PathVariable UUID id,
            @RequestBody AdminDtos.UserStatusRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.updateUserStatus(id, req, admin)));
    }

    // ─── ARTISTS ─────────────────────────────────────────────

    @GetMapping("/artists")
    public ResponseEntity<ApiResponse<Page<AdminDtos.AdminArtistDto>>> getArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminDtos.AdminArtistDto> artists = adminService.getAllArtists(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(artists));
    }

    @GetMapping("/artists/{id}")
    public ResponseEntity<ApiResponse<AdminDtos.AdminArtistDto>> getArtist(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getArtistById(id)));
    }

    @PatchMapping("/artists/{id}/approve")
    public ResponseEntity<ApiResponse<AdminDtos.AdminArtistDto>> approveArtist(
            @PathVariable UUID id,
            @RequestBody AdminDtos.ApproveArtistRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approveArtist(id, req, admin)));
    }

    @PatchMapping("/artists/{id}/feature")
    public ResponseEntity<ApiResponse<AdminDtos.AdminArtistDto>> featureArtist(
            @PathVariable UUID id,
            @RequestBody AdminDtos.FeatureArtistRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.featureArtist(id, req, admin)));
    }

    @DeleteMapping("/artists/{artistId}/portfolio/{itemId}")
    public ResponseEntity<ApiResponse<String>> deletePortfolioItem(
            @PathVariable UUID artistId,
            @PathVariable UUID itemId,
            @AuthenticationPrincipal User admin) {
        adminService.deleteArtistPortfolioItem(artistId, itemId, admin);
        return ResponseEntity.ok(ApiResponse.success("Portfolio item removed"));
    }

    // ─── ORDERS ──────────────────────────────────────────────

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<AdminDtos.AdminOrderDto>>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminDtos.AdminOrderDto> orders = adminService.getAllOrders(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<AdminDtos.AdminOrderDto>> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(adminService.getOrderById(id)));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<AdminDtos.AdminOrderDto>> overrideOrderStatus(
            @PathVariable UUID id,
            @RequestBody AdminDtos.AdminOrderStatusRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.overrideOrderStatus(id, req, admin)));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<String>> deleteOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {
        adminService.deleteOrder(id, admin);
        return ResponseEntity.ok(ApiResponse.success("Order deleted"));
    }

    // ─── REVIEWS ─────────────────────────────────────────────

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<Page<AdminDtos.AdminReviewDto>>> getReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminDtos.AdminReviewDto> reviews = adminService.getAllReviews(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<ApiResponse<String>> deleteReview(
            @PathVariable UUID id,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal User admin) {
        adminService.deleteReview(id, reason, admin);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }

    // ─── PAYOUTS ─────────────────────────────────────────────

    @GetMapping("/payouts")
    public ResponseEntity<ApiResponse<Page<AdminDtos.AdminPayoutDto>>> getPayouts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminDtos.AdminPayoutDto> payouts = adminService.getAllPayouts(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }

    @PatchMapping("/payouts/{id}/approve")
    public ResponseEntity<ApiResponse<AdminDtos.AdminPayoutDto>> approvePayout(
            @PathVariable UUID id,
            @RequestBody AdminDtos.PayoutActionRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.approvePayout(id, req, admin)));
    }

    @PatchMapping("/payouts/{id}/reject")
    public ResponseEntity<ApiResponse<AdminDtos.AdminPayoutDto>> rejectPayout(
            @PathVariable UUID id,
            @RequestBody AdminDtos.PayoutActionRequest req,
            @AuthenticationPrincipal User admin) {
        return ResponseEntity.ok(ApiResponse.success(adminService.rejectPayout(id, req, admin)));
    }

    // ─── NOTIFICATIONS ────────────────────────────────────────

    @PostMapping("/notifications/broadcast")
    public ResponseEntity<ApiResponse<String>> broadcastNotification(
            @RequestBody AdminDtos.BroadcastNotificationRequest req,
            @AuthenticationPrincipal User admin) {
        int count = adminService.broadcastNotification(req, admin);
        return ResponseEntity.ok(ApiResponse.success("Notification sent to " + count + " users"));
    }

    // ─── ANALYTICS ───────────────────────────────────────────

    @GetMapping("/analytics/summary")
    public ResponseEntity<ApiResponse<AdminDtos.PlatformAnalyticsDto>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.success(adminService.getAnalytics()));
    }

    // ─── AUDIT LOG ────────────────────────────────────────────

    @GetMapping("/audit-log")
    public ResponseEntity<ApiResponse<Page<AdminDtos.AuditLogDto>>> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.getAuditLog(PageRequest.of(page, size))));
    }
}

