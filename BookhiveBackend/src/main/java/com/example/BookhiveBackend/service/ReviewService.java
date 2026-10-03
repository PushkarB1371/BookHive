package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.CreateReviewRequest;
import com.example.BookhiveBackend.dto.response.ReviewResponse;
import com.example.BookhiveBackend.entity.Book;
import com.example.BookhiveBackend.entity.Review;
import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.enums.NotificationType;
import com.example.BookhiveBackend.repository.ReviewRepository;
import com.example.BookhiveBackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookService bookService;
    private final NotificationProducer notificationProducer;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository,
                         BookService bookService, NotificationProducer notificationProducer) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookService = bookService;
        this.notificationProducer = notificationProducer;
    }

    @Transactional
    public ReviewResponse addReview(UUID bookId, CreateReviewRequest request, UUID userId) {
        if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Book book = bookService.getBookEntity(bookId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (reviewRepository.findByBookId(bookId).stream().anyMatch(r -> r.getUser().getId().equals(userId))) {
            throw new IllegalArgumentException("You already reviewed this book");
        }

        Review review = Review.builder()
                .user(user)
                .book(book)
                .rating(request.rating())
                .reviewText(request.reviewText())
                .build();

        reviewRepository.save(review);

        if (book.getAddedBy() != null && !book.getAddedBy().getId().equals(userId)) {
            String message = user.getName() + " reviewed " + book.getTitle() + " (" + request.rating() + " stars)";
            notificationProducer.publish(book.getAddedBy(), NotificationType.NEW_REPLY, message);
        }

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForBook(UUID bookId) {
        return reviewRepository.findByBookId(bookId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Double getAverageRating(UUID bookId) {
        List<Review> reviews = reviewRepository.findByBookId(bookId);
        if (reviews.isEmpty()) return null;
        return reviews.stream().mapToInt(Review::getRating).average().orElse(0);
    }

    private ReviewResponse toResponse(Review r) {
        return new ReviewResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getName(),
                r.getRating(),
                r.getReviewText(),
                r.getCreatedAt()
        );
    }
}