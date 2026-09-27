package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.CreateBookRequest;
import com.example.BookhiveBackend.dto.response.BookResponse;
import com.example.BookhiveBackend.entity.Book;
import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.enums.UserRole;
import com.example.BookhiveBackend.repository.BookRepository;
import com.example.BookhiveBackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public BookService(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public BookResponse createBook(CreateBookRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admins can add books");
        }

        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .totalChapters(request.totalChapters())
                .coverImageUrl(request.coverImageUrl())
                .build();

        bookRepository.save(book);
        return toResponse(book);
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    public BookResponse getBookById(UUID id) {
        Book book = getBookEntity(id);
        return toResponse(book);
    }

    public Book getBookEntity(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(),
                book.getTotalChapters(), book.getCoverImageUrl());
    }
}