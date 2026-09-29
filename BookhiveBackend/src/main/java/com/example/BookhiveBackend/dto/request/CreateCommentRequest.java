package com.example.BookhiveBackend.dto.request;

public record CreateCommentRequest(Integer chapterNumber, String content) {}