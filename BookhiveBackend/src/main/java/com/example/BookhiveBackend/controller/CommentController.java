package com.example.BookhiveBackend.controller;

import com.example.BookhiveBackend.dto.request.CreateCommentRequest;
import com.example.BookhiveBackend.security.CurrentUser;
import com.example.BookhiveBackend.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/clubs/{clubId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable UUID clubId, @RequestBody CreateCommentRequest request) {
        try {
            return ResponseEntity.ok(commentService.addComment(clubId, request, CurrentUser.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getComments(
            @PathVariable UUID clubId,
            @RequestParam Integer chapter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(commentService.getCommentsPaged(clubId, chapter, page, size));
    }
}