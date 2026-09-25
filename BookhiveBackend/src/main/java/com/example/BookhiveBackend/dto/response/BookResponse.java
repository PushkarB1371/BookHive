package com.example.BookhiveBackend.dto.response;

import java.util.UUID;

public record BookResponse(UUID id, String title, String author, Integer totalChapters, String coverImageUrl) {}