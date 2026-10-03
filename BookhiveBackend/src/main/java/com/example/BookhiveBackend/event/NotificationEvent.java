package com.example.BookhiveBackend.event;

import com.example.BookhiveBackend.enums.NotificationType;

import java.util.UUID;

public record NotificationEvent(
        UUID recipientId,
        NotificationType type,
        String message
) {}