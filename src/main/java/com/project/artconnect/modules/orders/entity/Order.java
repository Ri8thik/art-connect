package com.project.artconnect.modules.orders.entity;

import com.project.artconnect.common.enums.OrderStatus;
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
@Table(name = "orders", schema = "app")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private User artist;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String category;

    @ElementCollection
    @CollectionTable(
            name = "order_reference_images",
            schema = "app",
            joinColumns = @JoinColumn(name = "order_id")
    )
    @Column(name = "image_url", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> referenceImageUrls = new ArrayList<>();

    // Budget
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String budgetCurrency;

    private LocalDateTime deadline;

    // Agreed pricing (set after acceptance)
    private BigDecimal agreedPrice;
    private BigDecimal platformFee;
    private BigDecimal artistNet;
    private String pricingCurrency;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC")
    @Builder.Default
    private List<StatusHistoryEntry> statusHistory = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

