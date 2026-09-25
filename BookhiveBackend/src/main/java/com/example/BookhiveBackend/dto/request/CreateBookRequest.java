package com.example.BookhiveBackend.dto.request;

public record CreateBookRequest(String title, String author, Integer totalChapters, String coverImageUrl) {}