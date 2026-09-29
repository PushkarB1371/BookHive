package com.example.BookhiveBackend.service;

import com.example.BookhiveBackend.dto.request.CreateCommentRequest;
import com.example.BookhiveBackend.dto.response.CommentResponse;
import com.example.BookhiveBackend.entity.Club;
import com.example.BookhiveBackend.entity.Comment;
import com.example.BookhiveBackend.entity.User;
import com.example.BookhiveBackend.repository.ClubRepository;
import com.example.BookhiveBackend.repository.CommentRepository;
import com.example.BookhiveBackend.repository.MembershipRepository;
import com.example.BookhiveBackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ClubRepository clubRepository;
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    public CommentService(CommentRepository commentRepository, ClubRepository clubRepository,
                          UserRepository userRepository, MembershipRepository membershipRepository) {
        this.commentRepository = commentRepository;
        this.clubRepository = clubRepository;
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
    }

    public CommentResponse addComment(UUID clubId, CreateCommentRequest request, UUID userId) {
        if (!membershipRepository.existsByUserIdAndClubId(userId, clubId)) {
            throw new IllegalArgumentException("Not a member of this club");
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new IllegalArgumentException("Club not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }

        Comment comment = Comment.builder()
                .user(user)
                .club(club)
                .chapterNumber(request.chapterNumber())
                .content(request.content())
                .build();

        commentRepository.save(comment);
        return toResponse(comment);
    }

    public List<CommentResponse> getComments(UUID clubId, Integer chapterNumber) {
        return commentRepository
                .findByClubIdAndChapterNumberOrderByCreatedAtAsc(clubId, chapterNumber)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getUser().getId(),
                c.getUser().getName(),
                c.getChapterNumber(),
                c.getContent(),
                c.getCreatedAt()
        );
    }
}