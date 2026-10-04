package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.CreateReviewRequest;
import com.example.BookhiveBackend.entity.Book;
import com.example.BookhiveBackend.entity.Review;
import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.repository.ReviewRepository;
import com.example.BookhiveBackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookService bookService;
    @Mock private NotificationProducer notificationProducer;

    @InjectMocks
    private ReviewService reviewService;

    private User reviewer;
    private Book book;

    @BeforeEach
    void setUp() {
        reviewer = User.builder().id(UUID.randomUUID()).name("Reviewer").build();
        book = Book.builder().id(UUID.randomUUID()).title("Some Book").build();
    }

    @Test
    void addReview_invalidRating_throws() {
        CreateReviewRequest request = new CreateReviewRequest(6, "Too high");

        assertThatThrownBy(() -> reviewService.addReview(book.getId(), request, reviewer.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void addReview_duplicateReview_throws() {
        when(bookService.getBookEntity(book.getId())).thenReturn(book);
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));

        Review existing = Review.builder().user(reviewer).book(book).rating(4).build();
        when(reviewRepository.findByBookId(book.getId())).thenReturn(List.of(existing));

        CreateReviewRequest request = new CreateReviewRequest(5, "Again");

        assertThatThrownBy(() -> reviewService.addReview(book.getId(), request, reviewer.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already reviewed");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_valid_savesAndNotifiesAdmin() {
        User admin = User.builder().id(UUID.randomUUID()).name("Admin").build();
        book.setAddedBy(admin);

        when(bookService.getBookEntity(book.getId())).thenReturn(book);
        when(userRepository.findById(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(reviewRepository.findByBookId(book.getId())).thenReturn(List.of());
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateReviewRequest request = new CreateReviewRequest(5, "Loved it");

        var response = reviewService.addReview(book.getId(), request, reviewer.getId());

        assertThat(response.rating()).isEqualTo(5);
        verify(notificationProducer).publish(eq(admin), any(), any());
    }

    @Test
    void getAverageRating_noReviews_returnsNull() {
        when(reviewRepository.findByBookId(book.getId())).thenReturn(List.of());

        Double avg = reviewService.getAverageRating(book.getId());

        assertThat(avg).isNull();
    }

    @Test
    void getAverageRating_withReviews_computesCorrectly() {
        Review r1 = Review.builder().rating(4).build();
        Review r2 = Review.builder().rating(2).build();
        when(reviewRepository.findByBookId(book.getId())).thenReturn(List.of(r1, r2));

        Double avg = reviewService.getAverageRating(book.getId());

        assertThat(avg).isEqualTo(3.0);
    }
}