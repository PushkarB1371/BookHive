package com.example.BookhiveBackend.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID userId,
        String userName,
        Integer rating,
        String reviewText,
        LocalDateTime createdAt
) {}