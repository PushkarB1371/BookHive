package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.CreateClubRequest;
import com.example.BookhiveBackend.dto.response.BookResponse;
import com.example.BookhiveBackend.dto.response.ClubResponse;
import com.example.BookhiveBackend.entity.Book;
import com.example.BookhiveBackend.entity.Club;
import com.example.BookhiveBackend.entity.Membership;
import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.enums.ClubRole;
import com.example.BookhiveBackend.enums.NotificationType;
import com.example.BookhiveBackend.repository.ClubRepository;
import com.example.BookhiveBackend.repository.CommentRepository;
import com.example.BookhiveBackend.repository.MembershipRepository;
import com.example.BookhiveBackend.repository.ProgressRepository;
import com.example.BookhiveBackend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final CommentRepository commentRepository;
    private final ProgressRepository progressRepository;
    private final NotificationService notificationService;

    public ClubService(ClubRepository clubRepository, UserRepository userRepository,
                       MembershipRepository membershipRepository, BookService bookService,
                       CommentRepository commentRepository, ProgressRepository progressRepository,
                       NotificationService notificationService) {
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.bookService = bookService;
        this.commentRepository = commentRepository;
        this.progressRepository = progressRepository;
        this.notificationService = notificationService;
    }

    @Transactional
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

        return toResponse(club, userId);
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getAllClubs(UUID userId) {
        return clubRepository.findAll().stream().map(c -> toResponse(c, userId)).toList();
    }

    @Transactional(readOnly = true)
    public ClubResponse getClubById(UUID id, UUID userId) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));
        return toResponse(club, userId);
    }

    @Transactional
    public ClubResponse setCurrentBook(UUID clubId, UUID bookId, UUID userId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));

        requireClubAdmin(clubId, userId);

        Book book = bookService.getBookEntity(bookId);
        club.setCurrentBook(book);
        clubRepository.save(club);

        List<Membership> members = membershipRepository.findByClubId(clubId);
        String message = club.getName() + " is now reading " + book.getTitle();
        for (Membership m : members) {
            if (!m.getUser().getId().equals(userId)) {
                notificationService.createNotification(m.getUser(), NotificationType.CHAPTER_UNLOCK, message);
            }
        }

        return toResponse(club, userId);
    }

    @Transactional
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

        if (!club.getCreatedBy().getId().equals(userId)) {
            String message = user.getName() + " joined " + club.getName();
            notificationService.createNotification(club.getCreatedBy(), NotificationType.NEW_REPLY, message);
        }
    }

    @Transactional
    public void deleteClub(UUID clubId, UUID userId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));

        if (!club.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the club creator can delete this club");
        }

        commentRepository.deleteByClubId(clubId);
        progressRepository.deleteByClubId(clubId);
        membershipRepository.deleteByClubId(clubId);
        clubRepository.delete(club);
    }

    @Transactional(readOnly = true)
    public Map<String, List<ClubResponse>> getClubsGroupedForUser(UUID userId) {
        List<Club> allClubs = clubRepository.findAll();
        List<Membership> myMemberships = membershipRepository.findByUserId(userId);

        Set<UUID> joinedClubIds = myMemberships.stream()
                .map(m -> m.getClub().getId())
                .collect(Collectors.toSet());

        List<ClubResponse> created = allClubs.stream()
                .filter(c -> c.getCreatedBy().getId().equals(userId))
                .map(c -> toResponse(c, userId))
                .toList();

        List<ClubResponse> joined = allClubs.stream()
                .filter(c -> joinedClubIds.contains(c.getId()) && !c.getCreatedBy().getId().equals(userId))
                .map(c -> toResponse(c, userId))
                .toList();

        List<ClubResponse> discover = allClubs.stream()
                .filter(c -> !joinedClubIds.contains(c.getId()) && !c.getCreatedBy().getId().equals(userId))
                .map(c -> toResponse(c, userId))
                .toList();

        Map<String, List<ClubResponse>> result = new LinkedHashMap<>();
        result.put("myClubs", created);
        result.put("joinedClubs", joined);
        result.put("discoverClubs", discover);
        return result;
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> getClubsReadingBook(UUID bookId, UUID userId) {
        return clubRepository.findAll().stream()
                .filter(c -> c.getCurrentBook() != null && c.getCurrentBook().getId().equals(bookId))
                .map(c -> toResponse(c, userId))
                .toList();
    }

    private void requireClubAdmin(UUID clubId, UUID userId) {
        Membership membership = membershipRepository.findByUserIdAndClubId(userId, clubId)
                .orElseThrow(() -> new IllegalArgumentException("Not a member of this club"));

        if (membership.getRoleInClub() != ClubRole.ADMIN) {
            throw new IllegalArgumentException("Only club admins can do this");
        }
    }

    private ClubResponse toResponse(Club club, UUID userId) {
        BookResponse bookResponse = null;
        if (club.getCurrentBook() != null) {
            Book b = club.getCurrentBook();
            bookResponse = new BookResponse(
                    b.getId(), b.getTitle(), b.getAuthor(), b.getTotalChapters(),
                    b.getCoverImageUrl(), b.getDescription(), b.getCategory(), b.getPublishedDate()
            );
        }

        Membership membership = membershipRepository.findByUserIdAndClubId(userId, club.getId()).orElse(null);
        boolean isAdmin = membership != null && membership.getRoleInClub() == ClubRole.ADMIN;
        boolean isMember = membership != null;

        return new ClubResponse(
                club.getId(),
                club.getName(),
                club.getDescription(),
                club.getCoverImageUrl(),
                bookResponse,
                club.getCreatedBy().getId(),
                club.getCreatedBy().getName(),
                isAdmin,
                isMember
        );
    }
}