package com.project.artconnect.modules.notifications.service;

import com.project.artconnect.common.enums.NotificationType;
import com.project.artconnect.modules.notifications.dto.NotificationDto;
import com.project.artconnect.modules.notifications.entity.Notification;
import com.project.artconnect.modules.notifications.repository.NotificationRepository;
import com.project.artconnect.modules.orders.entity.Order;
import com.project.artconnect.modules.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(User user, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationDto markAsRead(User user, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot mark this notification as read");
        }

        notification.setRead(true);
        return mapToDto(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsReadByRecipientId(user.getId());
    }

    // Internal methods for creating notifications

    @Transactional
    public void notifyNewOrder(Order order) {
        String customerName = order.getCustomer().getDisplayName() != null
                ? order.getCustomer().getDisplayName() : order.getCustomer().getEmail();

        createNotification(
                order.getArtist(),
                NotificationType.ORDER_NEW,
                "New Commission Request",
                "New request received from " + customerName + ": " + order.getTitle(),
                order.getId().toString(),
                order.getArtist().getId().toString()
        );
    }

    @Transactional
    public void notifyOrderStatusChange(Order order) {
        NotificationType type = NotificationType.ORDER_STATUS_CHANGED;
        String artistName = order.getArtist().getDisplayName() != null
                ? order.getArtist().getDisplayName() : order.getArtist().getEmail();
        String title;
        String body;
        User recipient = order.getCustomer();

        switch (order.getStatus()) {
            case ACCEPTED -> {
                type = NotificationType.ORDER_ACCEPTED;
                title = "Order Accepted";
                body = artistName + " accepted your request: " + order.getTitle();
            }
            case REJECTED -> {
                type = NotificationType.ORDER_REJECTED;
                title = "Order Rejected";
                body = artistName + " rejected your request: " + order.getTitle();
            }
            case IN_PROGRESS -> {
                type = NotificationType.ORDER_IN_PROGRESS;
                title = "Order In Progress";
                body = "Your order is now in progress: " + order.getTitle();
            }
            case COMPLETED -> {
                type = NotificationType.ORDER_COMPLETED;
                title = "Order Completed";
                body = "Customer confirmed completion for: " + order.getTitle();
                recipient = order.getArtist();
            }
            case DELIVERED -> {
                type = NotificationType.ORDER_DELIVERED;
                title = "Order Delivered!";
                body = "Your order was delivered: " + order.getTitle() + ". Leave a review!";
            }
            default -> {
                title = "Order Update";
                body = "Your order status changed to: " + order.getStatus();
            }
        }

        createNotification(
                recipient,
                type,
                title,
                body,
                order.getId().toString(),
                order.getArtist().getId().toString()
        );
    }

    private void createNotification(User recipient, NotificationType type, String title, String body,
                                     String orderId, String artistId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .body(body)
                .relatedOrderId(orderId)
                .relatedArtistId(artistId)
                .read(false)
                .build();
        notificationRepository.save(notification);
        log.debug("Notification created for user {}: {}", recipient.getEmail(), title);
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .relatedOrderId(notification.getRelatedOrderId())
                .relatedArtistId(notification.getRelatedArtistId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

