package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.CreateBookRequest;
import com.example.BookhiveBackend.dto.response.BookResponse;
import com.example.BookhiveBackend.entity.Book;
import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.enums.UserRole;
import com.example.BookhiveBackend.repository.BookRepository;
import com.example.BookhiveBackend.repository.ClubRepository;
import com.example.BookhiveBackend.repository.ReviewRepository;
import com.example.BookhiveBackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ReviewRepository reviewRepository;

    public BookService(BookRepository bookRepository, UserRepository userRepository,
                       ClubRepository clubRepository, ReviewRepository reviewRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
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
                .description(request.description())
                .category(request.category())
                .publishedDate(request.publishedDate())
                .addedBy(user)
                .build();

        bookRepository.save(book);
        return toResponse(book);
    }

    @Transactional(readOnly = true)
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse getBookById(UUID id) {
        Book book = getBookEntity(id);
        return toResponse(book);
    }

    @Transactional(readOnly = true)
    public Book getBookEntity(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));
    }

    @Transactional
    public void deleteBook(UUID bookId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only admins can delete books");
        }

        Book book = getBookEntity(bookId);

        boolean inUse = clubRepository.findAll().stream()
                .anyMatch(c -> c.getCurrentBook() != null && c.getCurrentBook().getId().equals(bookId));

        if (inUse) {
            throw new IllegalArgumentException("Can't delete a book that's currently being read by a club");
        }

        reviewRepository.deleteByBookId(bookId);
        bookRepository.delete(book);
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getTotalChapters(),
                book.getCoverImageUrl(),
                book.getDescription(),
                book.getCategory(),
                book.getPublishedDate()
        );
    }
}