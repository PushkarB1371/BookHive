package com.example.BookhiveBackend.controller;

import com.example.BookhiveBackend.dto.request.UpdateProgressRequest;
import com.example.BookhiveBackend.security.CurrentUser;
import com.example.BookhiveBackend.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clubs/{clubId}/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMyProgress(@PathVariable UUID clubId, @RequestBody UpdateProgressRequest request) {
        try {
            return ResponseEntity.ok(progressService.updateProgress(clubId, request, CurrentUser.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyProgress(@PathVariable UUID clubId) {
        return ResponseEntity.ok(progressService.getMyProgress(clubId, CurrentUser.getId()));
    }

    @GetMapping
    public ResponseEntity<?> getClubProgress(@PathVariable UUID clubId) {
        try {
            return ResponseEntity.ok(progressService.getClubProgress(clubId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}