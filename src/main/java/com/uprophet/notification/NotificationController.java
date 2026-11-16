package com.uprophet.notification;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.uprophet.auth.security.AuthUser;
import com.uprophet.notification.dto.NotificationCountResponse;
import com.uprophet.notification.dto.NotificationResponse;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public List<NotificationResponse> notifications(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return notificationService.notifications(user.username());
    }

    @GetMapping("/getNotificationCount")
    public NotificationCountResponse notificationCount(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return new NotificationCountResponse(notificationService.unreadCount(user.username()));
    }
}
