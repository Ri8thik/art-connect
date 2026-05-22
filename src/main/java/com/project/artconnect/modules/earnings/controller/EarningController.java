package com.project.artconnect.modules.earnings.controller;

import com.project.artconnect.common.response.ApiResponse;
import com.project.artconnect.modules.earnings.dto.EarningDto;
import com.project.artconnect.modules.earnings.dto.EarningsSummaryDto;
import com.project.artconnect.modules.earnings.service.EarningService;
import com.project.artconnect.modules.users.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/earnings")
@RequiredArgsConstructor
@Tag(name = "Earnings", description = "Artist earnings dashboard and ledger")
@SecurityRequirement(name = "bearerAuth")
public class EarningController {

    private final EarningService earningService;

    @GetMapping
    @Operation(summary = "Get earnings ledger for current artist")
    public ResponseEntity<ApiResponse<Page<EarningDto>>> getEarnings(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<EarningDto> earnings = earningService.getEarnings(user,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(ApiResponse.success(earnings));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get earnings summary (dashboard metrics)")
    public ResponseEntity<ApiResponse<EarningsSummaryDto>> getEarningsSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(earningService.getEarningsSummary(user)));
    }
}

