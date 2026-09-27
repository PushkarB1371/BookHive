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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .coverImageUrl(request.coverImageUrl())
                .createdBy(user)
                .build();
        clubRepository.save(club);

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

    public Map<String, List<ClubResponse>> getClubsGroupedForUser(UUID userId) {
        List<Club> allClubs = clubRepository.findAll();
        List<Membership> myMemberships = membershipRepository.findByUserId(userId);

        Set<UUID> joinedClubIds = myMemberships.stream()
                .map(m -> m.getClub().getId())
                .collect(Collectors.toSet());

        List<ClubResponse> created = allClubs.stream()
                .filter(c -> c.getCreatedBy().getId().equals(userId))
                .map(this::toResponse)
                .toList();

        List<ClubResponse> joined = allClubs.stream()
                .filter(c -> joinedClubIds.contains(c.getId()) && !c.getCreatedBy().getId().equals(userId))
                .map(this::toResponse)
                .toList();

        List<ClubResponse> discover = allClubs.stream()
                .filter(c -> !joinedClubIds.contains(c.getId()) && !c.getCreatedBy().getId().equals(userId))
                .map(this::toResponse)
                .toList();

        Map<String, List<ClubResponse>> result = new LinkedHashMap<>();
        result.put("myClubs", created);
        result.put("joinedClubs", joined);
        result.put("discoverClubs", discover);
        return result;
    }

    public List<ClubResponse> getClubsReadingBook(UUID bookId) {
        return clubRepository.findAll().stream()
                .filter(c -> c.getCurrentBook() != null && c.getCurrentBook().getId().equals(bookId))
                .map(this::toResponse)
                .toList();
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
                club.getCoverImageUrl(),
                bookResponse,
                club.getCreatedBy().getId(),
                club.getCreatedBy().getName()
        );
    }
}