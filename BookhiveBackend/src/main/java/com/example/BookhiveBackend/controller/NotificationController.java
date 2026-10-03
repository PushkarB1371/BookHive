package com.example.BookhiveBackend.controller;

import com.example.BookhiveBackend.security.CurrentUser;
import com.example.BookhiveBackend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications(CurrentUser.getId()));
    }

    @GetMapping("/me/unread-count")
    public ResponseEntity<?> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(CurrentUser.getId())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable UUID id) {
        try {
            notificationService.markAsRead(id, CurrentUser.getId());
            return ResponseEntity.ok(Map.of("message", "Marked as read"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead() {
        notificationService.markAllAsRead(CurrentUser.getId());
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable UUID id) {
        try {
            notificationService.deleteNotification(id, CurrentUser.getId());
            return ResponseEntity.ok(Map.of("message", "Notification removed"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}