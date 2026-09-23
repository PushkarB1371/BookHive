package com.example.BookhiveBackend.repository;



import com.example.BookhiveBackend.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, UUID> {
    // Finds all clubs created by the user with the given user ID.
    List<Club> findByCreatedById(UUID userId);
}