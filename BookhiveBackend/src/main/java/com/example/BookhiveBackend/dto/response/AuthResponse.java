package com.example.BookhiveBackend.dto.response;

import java.util.UUID;

public record AuthResponse(String token, UUID userId, String name, String email, String role) {}