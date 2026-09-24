package com.swiftbite.notification.dto;

import com.swiftbite.notification.entity.NotificationType;
import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private Long userId;
    private String orderId;
    private NotificationType type;
    private String title;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
