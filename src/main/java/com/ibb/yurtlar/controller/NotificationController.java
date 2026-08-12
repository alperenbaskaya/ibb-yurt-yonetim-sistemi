package com.ibb.yurtlar.controller;

import com.ibb.yurtlar.dto.NotificationResponse;
import com.ibb.yurtlar.dto.NotificationPageResponse;
import com.ibb.yurtlar.dto.NotificationUnreadCountResponse;
import com.ibb.yurtlar.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService
            notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService =
                notificationService;
    }

    @GetMapping("/me")
    public NotificationPageResponse
    getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return notificationService
                .getMyNotifications(
                        authentication.getName(), page, size
                );
    }

    @GetMapping("/me/unread")
    public NotificationPageResponse
    getMyUnreadNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return notificationService
                .getMyUnreadNotifications(
                        authentication.getName(), page, size
                );
    }

    @GetMapping("/me/unread-count")
    public NotificationUnreadCountResponse
    getMyUnreadCount(
            Authentication authentication
    ) {
        return notificationService
                .getMyUnreadCount(
                        authentication.getName()
                );
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        return notificationService
                .markAsRead(
                        notificationId,
                        authentication.getName()
                );
    }

    @PatchMapping("/me/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(
            Authentication authentication
    ) {
        notificationService
                .markAllAsRead(
                        authentication.getName()
                );
    }
}
