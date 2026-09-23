package com.example.BookhiveBackend.repository;


import com.example.BookhiveBackend.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgressRepository extends JpaRepository<Progress, UUID> {
    Optional<Progress> findByUserIdAndClubIdAndBookId(UUID userId, UUID clubId, UUID bookId);
    List<Progress> findByClubIdAndBookId(UUID clubId, UUID bookId);
}
