package com.example.BookhiveBackend.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class CurrentUser {
    public static UUID getId() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return UUID.fromString(userId);
    }
}