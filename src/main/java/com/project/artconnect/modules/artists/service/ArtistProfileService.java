package com.project.artconnect.modules.artists.service;

import com.project.artconnect.common.enums.UserRole;
import com.project.artconnect.modules.artists.dto.AddPortfolioItemRequest;
import com.project.artconnect.modules.artists.dto.ArtistProfileDto;
import com.project.artconnect.modules.artists.dto.CreateArtistProfileRequest;
import com.project.artconnect.modules.artists.entity.ArtistProfile;
import com.project.artconnect.modules.artists.entity.PortfolioItem;
import com.project.artconnect.modules.artists.repository.ArtistProfileRepository;
import com.project.artconnect.modules.users.entity.User;
import com.project.artconnect.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistProfileService {

    private static final String DEFAULT_AVAILABILITY = "available";

    private final ArtistProfileRepository artistProfileRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ArtistProfileDto> searchArtists(String category, String city, String country, Pageable pageable) {
        return artistProfileRepository.searchArtists(category, city, country, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public ArtistProfileDto getArtistProfile(UUID artistId) {
        ArtistProfile profile = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new RuntimeException("Artist profile not found with id: " + artistId));
        return mapToDto(profile);
    }

    @Transactional(readOnly = true)
    public ArtistProfileDto getMyArtistProfile(User user) {
        ArtistProfile profile = artistProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("You don't have an artist profile yet"));
        return mapToDto(profile);
    }

    @Transactional
    public ArtistProfileDto createArtistProfile(User user, CreateArtistProfileRequest request) {
        if (artistProfileRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("Artist profile already exists for this user");
        }

        ArtistProfile profile = ArtistProfile.builder()
                .user(user)
                .bio(request.getBio())
                .skills(request.getSkills() != null ? request.getSkills() : java.util.List.of())
                .categories(request.getCategories() != null ? request.getCategories() : java.util.List.of())
                .startingPrice(request.getStartingPrice())
                .pricingCurrency(request.getPricingCurrency())
                .pricingNotes(request.getPricingNotes())
                .availability(normalizeAvailability(request.getAvailability()))
                .isActive(true)
                .build();

        profile = artistProfileRepository.save(profile);

        // Add portfolio items if provided
        if (request.getPortfolio() != null && !request.getPortfolio().isEmpty()) {
            for (CreateArtistProfileRequest.PortfolioItemInput portfolioInput : request.getPortfolio()) {
                PortfolioItem item = PortfolioItem.builder()
                        .artistProfile(profile)
                        .title(portfolioInput.getTitle())
                        .imageUrl(portfolioInput.getImageUrl())
                        .category(portfolioInput.getCategory())
                        .description(portfolioInput.getDescription())
                        .build();
                profile.getPortfolio().add(item);
            }
            profile = artistProfileRepository.save(profile);
        }

        // Grant ARTIST role
        user.getRoles().add(UserRole.ARTIST);
        userRepository.save(user);

        return mapToDto(profile);
    }

    @Transactional
    public ArtistProfileDto updateArtistProfile(User user, CreateArtistProfileRequest request) {
        ArtistProfile profile = artistProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Artist profile not found"));

        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getSkills() != null) profile.setSkills(request.getSkills());
        if (request.getCategories() != null) profile.setCategories(request.getCategories());
        if (request.getStartingPrice() != null) profile.setStartingPrice(request.getStartingPrice());
        if (request.getPricingCurrency() != null) profile.setPricingCurrency(request.getPricingCurrency());
        if (request.getPricingNotes() != null) profile.setPricingNotes(request.getPricingNotes());
        if (request.getAvailability() != null) profile.setAvailability(normalizeAvailability(request.getAvailability()));

        // Handle portfolio items - replace entire portfolio if provided
        if (request.getPortfolio() != null) {
            profile.getPortfolio().clear();
            for (CreateArtistProfileRequest.PortfolioItemInput portfolioInput : request.getPortfolio()) {
                PortfolioItem item = PortfolioItem.builder()
                        .artistProfile(profile)
                        .title(portfolioInput.getTitle())
                        .imageUrl(portfolioInput.getImageUrl())
                        .category(portfolioInput.getCategory())
                        .description(portfolioInput.getDescription())
                        .build();
                profile.getPortfolio().add(item);
            }
        }

        return mapToDto(artistProfileRepository.save(profile));
    }

    @Transactional
    public ArtistProfileDto addPortfolioItem(User user, AddPortfolioItemRequest request) {
        ArtistProfile profile = artistProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Artist profile not found"));

        PortfolioItem item = PortfolioItem.builder()
                .artistProfile(profile)
                .imageUrl(request.getImageUrl())
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .build();

        profile.getPortfolio().add(item);
        return mapToDto(artistProfileRepository.save(profile));
    }

    @Transactional
    public void deletePortfolioItem(User user, UUID itemId) {
        ArtistProfile profile = artistProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Artist profile not found"));

        profile.getPortfolio().removeIf(item -> item.getId().equals(itemId));
        artistProfileRepository.save(profile);
    }

    public ArtistProfileDto mapToDto(ArtistProfile profile) {
        var skills = profile.getSkills() != null ? new ArrayList<>(profile.getSkills()) : java.util.List.<String>of();
        var categories = profile.getCategories() != null ? new ArrayList<>(profile.getCategories()) : java.util.List.<String>of();
        var availability = resolveAvailability(profile);

        return ArtistProfileDto.builder()
                .id(profile.getId())
                .displayName(profile.getUser().getDisplayName())
                .email(profile.getUser().getEmail())
                .photoUrl(profile.getUser().getPhotoUrl())
                .locationCity(profile.getUser().getLocationCity())
                .locationState(profile.getUser().getLocationState())
                .locationCountry(profile.getUser().getLocationCountry())
                .bio(profile.getBio())
                .skills(skills)
                .categories(categories)
                .startingPrice(profile.getStartingPrice())
                .pricingCurrency(profile.getPricingCurrency())
                .pricingNotes(profile.getPricingNotes())
                .availability(availability)
                .ratingAvg(profile.getRatingAvg())
                .ratingCount(profile.getRatingCount())
                .isActive(profile.isActive())
                .portfolio(profile.getPortfolio().stream()
                        .map(item -> ArtistProfileDto.PortfolioItemDto.builder()
                                .id(item.getId())
                                .imageUrl(item.getImageUrl())
                                .title(item.getTitle())
                                .description(item.getDescription())
                                .category(item.getCategory())
                                .createdAt(item.getCreatedAt())
                                .build())
                        .toList())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private String resolveAvailability(ArtistProfile profile) {
        if (profile.getAvailability() != null && !profile.getAvailability().isBlank()) {
            return normalizeAvailability(profile.getAvailability());
        }

        String pricingNotes = profile.getPricingNotes();
        if (pricingNotes != null) {
            String normalizedNotes = pricingNotes.trim().toLowerCase();
            if (normalizedNotes.startsWith("availability:")) {
                String legacyValue = pricingNotes.substring(pricingNotes.indexOf(':') + 1).trim();
                return normalizeAvailability(legacyValue);
            }
        }

        return DEFAULT_AVAILABILITY;
    }

    private String normalizeAvailability(String availability) {
        if (availability == null || availability.isBlank()) {
            return DEFAULT_AVAILABILITY;
        }

        String normalized = availability.trim().toLowerCase();
        return switch (normalized) {
            case "busy", "unavailable" -> "busy";
            default -> DEFAULT_AVAILABILITY;
        };
    }
}

