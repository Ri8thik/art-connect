package com.project.artconnect.modules.orders.service;

import com.project.artconnect.common.enums.OrderStatus;
import com.project.artconnect.modules.earnings.service.EarningService;
import com.project.artconnect.modules.notifications.service.NotificationService;
import com.project.artconnect.modules.orders.dto.CreateOrderRequest;
import com.project.artconnect.modules.orders.dto.OrderDto;
import com.project.artconnect.modules.orders.dto.UpdateOrderStatusRequest;
import com.project.artconnect.modules.orders.entity.Order;
import com.project.artconnect.modules.orders.entity.StatusHistoryEntry;
import com.project.artconnect.modules.orders.repository.OrderRepository;
import com.project.artconnect.modules.users.entity.User;
import com.project.artconnect.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final EarningService earningService;

    // Valid state transitions enforced server-side
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.PENDING, EnumSet.of(OrderStatus.ACCEPTED, OrderStatus.REJECTED),
            OrderStatus.ACCEPTED, EnumSet.of(OrderStatus.IN_PROGRESS),
            // Artist delivers work first, then customer confirms completion.
            OrderStatus.IN_PROGRESS, EnumSet.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, EnumSet.of(OrderStatus.COMPLETED),
            OrderStatus.REJECTED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class)
    );

    @Transactional
    public OrderDto createOrder(User customer, CreateOrderRequest request) {
        User artist = userRepository.findById(request.getArtistId())
                .orElseThrow(() -> new RuntimeException("Artist not found with id: " + request.getArtistId()));

        if (!artist.isArtist()) {
            throw new RuntimeException("The specified user is not an artist");
        }

        Order order = Order.builder()
                .customer(customer)
                .artist(artist)
                .status(OrderStatus.PENDING)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .referenceImageUrls(request.getReferenceImageUrls() != null ? request.getReferenceImageUrls() : java.util.List.of())
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .budgetCurrency(request.getBudgetCurrency())
                .deadline(request.getDeadline())
                .build();

        StatusHistoryEntry initialEntry = StatusHistoryEntry.builder()
                .order(order)
                .status(OrderStatus.PENDING)
                .changedBy(customer.getEmail())
                .build();
        order.getStatusHistory().add(initialEntry);

        order = orderRepository.save(order);

        // Notify artist
        notificationService.notifyNewOrder(order);

        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getCustomerOrders(User customer, Pageable pageable) {
        return orderRepository.findByCustomerId(customer.getId(), pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getArtistOrders(User artist, Pageable pageable) {
        return orderRepository.findByArtistId(artist.getId(), pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(User user, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (!order.getCustomer().getId().equals(user.getId()) &&
            !order.getArtist().getId().equals(user.getId())) {
            throw new RuntimeException("You do not have permission to view this order");
        }

        return mapToDto(order);
    }

    @Transactional
    public OrderDto updateOrderStatus(User actor, UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus newStatus = request.getStatus();
        boolean isArtistActor = order.getArtist().getId().equals(actor.getId());
        boolean isCustomerActor = order.getCustomer().getId().equals(actor.getId());

        if (!isArtistActor && !isCustomerActor) {
            throw new RuntimeException("You do not have permission to update this order status");
        }

        // Only customer can confirm final completion, all other transitions are artist actions.
        if (newStatus == OrderStatus.COMPLETED) {
            if (!isCustomerActor) {
                throw new RuntimeException("Only the customer can confirm order completion");
            }
        } else if (!isArtistActor) {
            throw new RuntimeException("Only the assigned artist can update order status");
        }

        Set<OrderStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(order.getStatus(), EnumSet.noneOf(OrderStatus.class));

        if (!allowed.contains(newStatus)) {
            throw new RuntimeException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);

        // Set pricing if accepting the order
        if (newStatus == OrderStatus.ACCEPTED && request.getAgreedPrice() != null) {
            order.setAgreedPrice(request.getAgreedPrice());
            order.setPlatformFee(request.getPlatformFee() != null ? request.getPlatformFee() : BigDecimal.ZERO);
            order.setArtistNet(request.getAgreedPrice().subtract(order.getPlatformFee()));
            order.setPricingCurrency(request.getPricingCurrency());
        }

        StatusHistoryEntry entry = StatusHistoryEntry.builder()
                .order(order)
                .status(newStatus)
                .changedBy(actor.getEmail())
                .build();
        order.getStatusHistory().add(entry);

        order = orderRepository.save(order);

        // Notify customer about status change
        notificationService.notifyOrderStatusChange(order);

        // Create earning entry only after final customer confirmation.
        if (newStatus == OrderStatus.COMPLETED) {
            earningService.createEarningForOrder(order);
        }

        return mapToDto(order);
    }

    public OrderDto mapToDto(Order order) {
        var referenceImageUrls = order.getReferenceImageUrls() != null
                ? new ArrayList<>(order.getReferenceImageUrls())
                : java.util.List.<String>of();
        var statusHistory = order.getStatusHistory() != null
                ? new ArrayList<>(order.getStatusHistory())
                : java.util.List.<StatusHistoryEntry>of();

        return OrderDto.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getDisplayName())
                .customerPhotoUrl(order.getCustomer().getPhotoUrl())
                .artistId(order.getArtist().getId())
                .artistName(order.getArtist().getDisplayName())
                .artistPhotoUrl(order.getArtist().getPhotoUrl())
                .status(order.getStatus())
                .title(order.getTitle())
                .description(order.getDescription())
                .category(order.getCategory())
                .referenceImageUrls(referenceImageUrls)
                .budgetMin(order.getBudgetMin())
                .budgetMax(order.getBudgetMax())
                .budgetCurrency(order.getBudgetCurrency())
                .deadline(order.getDeadline())
                .agreedPrice(order.getAgreedPrice())
                .platformFee(order.getPlatformFee())
                .artistNet(order.getArtistNet())
                .pricingCurrency(order.getPricingCurrency())
                .statusHistory(statusHistory.stream()
                        .map(h -> OrderDto.StatusHistoryDto.builder()
                                .status(h.getStatus())
                                .changedBy(h.getChangedBy())
                                .changedAt(h.getChangedAt())
                                .build())
                        .toList())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

