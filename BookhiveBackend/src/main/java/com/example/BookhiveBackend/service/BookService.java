package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.CreateBookRequest;
import com.example.BookhiveBackend.dto.response.BookResponse;
import com.example.BookhiveBackend.entity.Book;
import com.example.BookhiveBackend.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse createBook(CreateBookRequest request) {
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

    public Book getBookEntity(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(),
                book.getTotalChapters(), book.getCoverImageUrl());
    }
}