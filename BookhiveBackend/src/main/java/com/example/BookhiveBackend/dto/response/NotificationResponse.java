package com.example.BookhiveBackend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String message,
        boolean isRead,
        LocalDateTime createdAt
) {}