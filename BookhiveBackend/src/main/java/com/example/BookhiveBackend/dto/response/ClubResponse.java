package com.example.BookhiveBackend.dto.response;

import java.util.UUID;

public record ClubResponse(
        UUID id,
        String name,
        String description,
        BookResponse currentBook,
        UUID createdBy,
        String createdByName
) {}