package com.example.BookhiveBackend.repository;


import com.example.BookhiveBackend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByClubIdAndChapterNumberOrderByCreatedAtAsc(UUID clubId, Integer chapterNumber);
}
