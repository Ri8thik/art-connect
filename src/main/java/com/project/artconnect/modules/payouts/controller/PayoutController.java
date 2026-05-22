package com.project.artconnect.modules.payouts.controller;

import com.project.artconnect.common.response.ApiResponse;
import com.project.artconnect.modules.payouts.dto.PayoutDto;
import com.project.artconnect.modules.payouts.service.PayoutService;
import com.project.artconnect.modules.users.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payouts")
@RequiredArgsConstructor
@Tag(name = "Payouts", description = "Artist payout management")
@SecurityRequirement(name = "bearerAuth")
public class PayoutController {

    private final PayoutService payoutService;

    @GetMapping
    @Operation(summary = "Get payout history for current artist")
    public ResponseEntity<ApiResponse<Page<PayoutDto>>> getPayouts(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PayoutDto> payouts = payoutService.getPayouts(user,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(payouts));
    }

    @PostMapping("/request")
    @Operation(summary = "Request a payout for all available earnings")
    public ResponseEntity<ApiResponse<PayoutDto>> requestPayout(@AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payout requested", payoutService.requestPayout(user)));
    }
}

