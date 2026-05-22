package com.project.artconnect.modules.artists.controller;

import com.project.artconnect.common.response.ApiResponse;
import com.project.artconnect.modules.artists.dto.AddPortfolioItemRequest;
import com.project.artconnect.modules.artists.dto.ArtistProfileDto;
import com.project.artconnect.modules.artists.dto.CreateArtistProfileRequest;
import com.project.artconnect.modules.artists.service.ArtistProfileService;
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
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
@Tag(name = "Artists", description = "Artist profile management and discovery")
@SecurityRequirement(name = "bearerAuth")
public class ArtistProfileController {

    private final ArtistProfileService artistProfileService;

    @GetMapping
    @Operation(summary = "Search and browse artists")
    public ResponseEntity<ApiResponse<Page<ArtistProfileDto>>> searchArtists(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "ratingAvg") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<ArtistProfileDto> artists = artistProfileService.searchArtists(category, city, country,
                PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(artists));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get artist profile by ID")
    public ResponseEntity<ApiResponse<ArtistProfileDto>> getArtistProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(artistProfileService.getArtistProfile(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my artist profile")
    public ResponseEntity<ApiResponse<ArtistProfileDto>> getMyArtistProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success(artistProfileService.getMyArtistProfile(user)));
    }

    @PostMapping
    @Operation(summary = "Create artist profile (upgrade to artist)")
    public ResponseEntity<ApiResponse<ArtistProfileDto>> createArtistProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateArtistProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Artist profile created successfully",
                        artistProfileService.createArtistProfile(user, request)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my artist profile")
    public ResponseEntity<ApiResponse<ArtistProfileDto>> updateArtistProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateArtistProfileRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Artist profile updated",
                artistProfileService.updateArtistProfile(user, request)));
    }

    @PostMapping("/me/portfolio")
    @Operation(summary = "Add portfolio item")
    public ResponseEntity<ApiResponse<ArtistProfileDto>> addPortfolioItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddPortfolioItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Portfolio item added",
                        artistProfileService.addPortfolioItem(user, request)));
    }

    @DeleteMapping("/me/portfolio/{itemId}")
    @Operation(summary = "Delete portfolio item")
    public ResponseEntity<ApiResponse<Void>> deletePortfolioItem(
            @AuthenticationPrincipal User user,
            @PathVariable UUID itemId
    ) {
        artistProfileService.deletePortfolioItem(user, itemId);
        return ResponseEntity.ok(ApiResponse.success("Portfolio item deleted"));
    }
}

