package com.example.BookhiveBackend.controller;

import com.example.BookhiveBackend.dto.request.CreateClubRequest;
import com.example.BookhiveBackend.security.CurrentUser;
import com.example.BookhiveBackend.service.ClubService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubService clubService;

    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }

    @PostMapping
    public ResponseEntity<?> createClub(@RequestBody CreateClubRequest request) {
        try {
            return ResponseEntity.ok(clubService.createClub(request, CurrentUser.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllClubs() {
        return ResponseEntity.ok(clubService.getAllClubs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClub(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(clubService.getClubById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinClub(@PathVariable UUID id) {
        try {
            clubService.joinClub(id, CurrentUser.getId());
            return ResponseEntity.ok(Map.of("message", "Joined club successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/current-book/{bookId}")
    public ResponseEntity<?> setCurrentBook(@PathVariable UUID id, @PathVariable UUID bookId) {
        try {
            return ResponseEntity.ok(clubService.setCurrentBook(id, bookId, CurrentUser.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}