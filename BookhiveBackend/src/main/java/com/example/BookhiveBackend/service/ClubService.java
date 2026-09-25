package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.CreateClubRequest;
import com.example.BookhiveBackend.dto.response.BookResponse;
import com.example.BookhiveBackend.dto.response.ClubResponse;
import com.example.BookhiveBackend.entity.Book;
import com.example.BookhiveBackend.entity.Club;
import com.example.BookhiveBackend.entity.Membership;
import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.enums.ClubRole;
import com.example.BookhiveBackend.repository.ClubRepository;
import com.example.BookhiveBackend.repository.MembershipRepository;
import com.example.BookhiveBackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final BookService bookService;

    public ClubService(ClubRepository clubRepository, UserRepository userRepository,
                       MembershipRepository membershipRepository, BookService bookService) {
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.bookService = bookService;
    }

    public ClubResponse createClub(CreateClubRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Club club = Club.builder()
                .name(request.name())
                .description(request.description())
                .createdBy(user)
                .build();
        clubRepository.save(club);

        // creator automatically becomes an admin member of their own club
        Membership membership = Membership.builder()
                .user(user)
                .club(club)
                .roleInClub(ClubRole.ADMIN)
                .build();
        membershipRepository.save(membership);

        return toResponse(club);
    }

    public List<ClubResponse> getAllClubs() {
        return clubRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ClubResponse getClubById(UUID id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));
        return toResponse(club);
    }

    public ClubResponse setCurrentBook(UUID clubId, UUID bookId, UUID userId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));

        requireClubAdmin(clubId, userId);

        Book book = bookService.getBookEntity(bookId);
        club.setCurrentBook(book);
        clubRepository.save(club);

        return toResponse(club);
    }

    public void joinClub(UUID clubId, UUID userId) {
        if (membershipRepository.existsByUserIdAndClubId(userId, clubId)) {
            throw new IllegalArgumentException("Already a member of this club");
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Membership membership = Membership.builder()
                .user(user)
                .club(club)
                .roleInClub(ClubRole.MEMBER)
                .build();
        membershipRepository.save(membership);
    }

    private void requireClubAdmin(UUID clubId, UUID userId) {
        Membership membership = membershipRepository.findByUserIdAndClubId(userId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Not a member of this club"));

        if (membership.getRoleInClub() != ClubRole.ADMIN) {
            throw new IllegalArgumentException("Only club admins can do this");
        }
    }

    private ClubResponse toResponse(Club club) {
        BookResponse bookResponse = null;
        if (club.getCurrentBook() != null) {
            Book b = club.getCurrentBook();
            bookResponse = new BookResponse(b.getId(), b.getTitle(), b.getAuthor(),
                    b.getTotalChapters(), b.getCoverImageUrl());
        }

        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getDescription(),
                bookResponse,
                club.getCreatedBy().getId(),
                club.getCreatedBy().getName()
        );
    }
}