package com.project.artconnect.modules.notifications.dto;

import com.project.artconnect.common.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationDto {
    private UUID id;
    private UUID recipientId;
    private NotificationType type;
    private String title;
    private String body;
    private String relatedOrderId;
    private String relatedArtistId;
    private boolean read;
    private LocalDateTime createdAt;
}

