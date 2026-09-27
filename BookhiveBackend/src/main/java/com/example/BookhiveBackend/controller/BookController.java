package com.example.BookhiveBackend.controller;

import com.example.BookhiveBackend.dto.request.CreateBookRequest;
import com.example.BookhiveBackend.security.CurrentUser;
import com.example.BookhiveBackend.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody CreateBookRequest request) {
        try {
            return ResponseEntity.ok(bookService.createBook(request, CurrentUser.getId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBook(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(bookService.getBookById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}