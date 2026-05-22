package com.project.artconnect.modules.artists.entity;

import com.project.artconnect.common.enums.ApprovalStatus;
import com.project.artconnect.modules.users.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "artist_profiles", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistProfile {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @ElementCollection
    @CollectionTable(
            name = "artist_skills",
            schema = "app",
            joinColumns = @JoinColumn(name = "artist_id")
    )
    @Column(name = "skill")
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "artist_categories",
            schema = "app",
            joinColumns = @JoinColumn(name = "artist_id")
    )
    @Column(name = "category")
    @Builder.Default
    private List<String> categories = new ArrayList<>();

    // Pricing
    private BigDecimal startingPrice;
    private String pricingCurrency;
    @Column(columnDefinition = "TEXT")
    private String pricingNotes;
    private String availability;

    // Rating summary
    @Builder.Default
    private Double ratingAvg = 0.0;
    @Builder.Default
    private Integer ratingCount = 0;

    @Builder.Default
    private boolean isActive = true;

    // Admin-managed fields
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApprovalStatus approvalStatus = ApprovalStatus.APPROVED;

    @Column(columnDefinition = "TEXT")
    private String approvalReason;

    @Builder.Default
    private boolean featured = false;

    private Integer featuredRank;

    @OneToMany(mappedBy = "artistProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<PortfolioItem> portfolio = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

