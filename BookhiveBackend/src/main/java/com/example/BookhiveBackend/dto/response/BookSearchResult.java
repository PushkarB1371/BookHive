package com.example.BookhiveBackend.dto.response;

public record BookSearchResult(
        String title,
        String author,
        String coverImageUrl,
        String description,
        String publishedDate,
        String category
) {}