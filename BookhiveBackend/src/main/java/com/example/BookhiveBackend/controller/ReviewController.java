package com.example.BookhiveBackend.controller;

import com.example.BookhiveBackend.dto.request.CreateReviewRequest;
import com.example.BookhiveBackend.security.CurrentUser;
import com.example.BookhiveBackend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/books/{bookId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> addReview(@PathVariable UUID bookId, @RequestBody CreateReviewRequest request) {
        try {
            return ResponseEntity.ok(reviewService.addReview(bookId, request, CurrentUser.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getReviews(@PathVariable UUID bookId) {
        Map<String, Object> response = new HashMap<>();
        response.put("reviews", reviewService.getReviewsForBook(bookId));
        response.put("averageRating", reviewService.getAverageRating(bookId));
        return ResponseEntity.ok(response);
    }
}