package com.swiftbite.notification.controller;

import com.swiftbite.notification.dto.NotificationResponse;
import com.swiftbite.notification.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final NotificationRepository notificationRepository;

    public AdminNotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        List<NotificationResponse> notifications = notificationRepository.findAll()
                .stream()
                .map(n -> {
                    NotificationResponse r = new NotificationResponse();
                    r.setId(n.getId());
                    r.setUserId(n.getUserId());
                    r.setOrderId(n.getOrderId());
                    r.setType(n.getType());
                    r.setTitle(n.getTitle());
                    r.setMessage(n.getMessage());
                    r.setRead(n.isRead());
                    r.setCreatedAt(n.getCreatedAt());
                    return r;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }
}
