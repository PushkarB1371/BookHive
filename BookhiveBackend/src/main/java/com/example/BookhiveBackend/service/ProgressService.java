package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.UpdateProgressRequest;
import com.example.BookhiveBackend.dto.response.ProgressResponse;
import com.example.BookhiveBackend.entity.Book;
import com.example.BookhiveBackend.entity.Club;
import com.example.BookhiveBackend.entity.Progress;
import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.repository.ClubRepository;
import com.example.BookhiveBackend.repository.MembershipRepository;
import com.example.BookhiveBackend.repository.ProgressRepository;
import com.example.BookhiveBackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public ProgressService(ProgressRepository progressRepository, ClubRepository clubRepository,
                           UserRepository userRepository, MembershipRepository membershipRepository) {
        this.progressRepository = progressRepository;
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    public ProgressResponse updateProgress(UUID clubId, UpdateProgressRequest request, UUID userId) {
        if (!membershipRepository.existsByUserIdAndClubId(userId, clubId)) {
            throw new IllegalArgumentException("Not a member of this club");
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));

        if (club.getCurrentBook() == null) {
            throw new IllegalArgumentException("This club has no current book set");
        }

        Book book = club.getCurrentBook();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.currentChapter() < 0 || request.currentChapter() > book.getTotalChapters()) {
            throw new IllegalArgumentException("Chapter out of range for this book");
        }

        Progress progress = progressRepository
                .findByUserIdAndClubIdAndBookId(userId, clubId, book.getId())
                .orElse(Progress.builder()
                        .user(user)
                        .club(club)
                        .book(book)
                        .currentChapter(0)
                        .build());

        progress.setCurrentChapter(request.currentChapter());
        progressRepository.save(progress);

        return toResponse(progress, book.getTotalChapters());
    }

    public List<ProgressResponse> getClubProgress(UUID clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));

        if (club.getCurrentBook() == null) {
            return List.of();
        }

        return progressRepository.findByClubIdAndBookId(clubId, club.getCurrentBook().getId())
                .stream()
                .map(p -> toResponse(p, club.getCurrentBook().getTotalChapters()))
                .toList();
    }

    public ProgressResponse getMyProgress(UUID clubId, UUID userId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));

        if (club.getCurrentBook() == null) {
            return null;
        }

        return progressRepository
                .findByUserIdAndClubIdAndBookId(userId, clubId, club.getCurrentBook().getId())
                .map(p -> toResponse(p, club.getCurrentBook().getTotalChapters()))
                .orElse(null);
    }

    private ProgressResponse toResponse(Progress p, Integer totalChapters) {
        return new ProgressResponse(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getName(),
                p.getBook().getId(),
                p.getCurrentChapter(),
                totalChapters,
                p.getUpdatedAt()
        );
    }
}