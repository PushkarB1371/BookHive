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
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    @Mock private ClubRepository clubRepository;
    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private BookService bookService;

    private User adminUser;
    private User memberUser;

    @BeforeEach
    void setUp() {
        adminUser = User.builder().id(UUID.randomUUID()).name("Admin").role(UserRole.ADMIN).build();
        memberUser = User.builder().id(UUID.randomUUID()).name("Member").role(UserRole.MEMBER).build();
    }

    @Test
    void createBook_asAdmin_savesAndReturnsBook() {
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateBookRequest request = new CreateBookRequest(
                "Atomic Habits", "James Clear", 20, null, null, null, null
        );

        BookResponse response = bookService.createBook(request, adminUser.getId());

        assertThat(response.title()).isEqualTo("Atomic Habits");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_asNonAdmin_throws() {
        when(userRepository.findById(memberUser.getId())).thenReturn(Optional.of(memberUser));

        CreateBookRequest request = new CreateBookRequest("Any Title", "Any Author", 10, null, null, null, null);

        assertThatThrownBy(() -> bookService.createBook(request, memberUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only admins can add books");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void getBookById_notFound_throws() {
        UUID missingId = UUID.randomUUID();
        when(bookRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(missingId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void deleteBook_whenInUseByClub_throws() {
        Book book = Book.builder().id(UUID.randomUUID()).title("In Use Book").build();
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));

        com.example.BookhiveBackend.entity.Club clubUsingBook =
                com.example.BookhiveBackend.entity.Club.builder().currentBook(book).build();
        when(clubRepository.findAll()).thenReturn(List.of(clubUsingBook));

        assertThatThrownBy(() -> bookService.deleteBook(book.getId(), adminUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("currently being read");

        verify(bookRepository, never()).delete(any());
    }

    @Test
    void getAllBooksPaged_slicesCorrectly() {
        List<Book> books = List.of(
                Book.builder().id(UUID.randomUUID()).title("Book A").build(),
                Book.builder().id(UUID.randomUUID()).title("Book B").build(),
                Book.builder().id(UUID.randomUUID()).title("Book C").build()
        );
        when(bookRepository.findAll()).thenReturn(books);

        var page = bookService.getAllBooksPaged(0, 2);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(3);
        assertThat(page.totalPages()).isEqualTo(2);
    }
}