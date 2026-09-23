package com.example.BookhiveBackend.repository;


import com.example.BookhiveBackend.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    // Finds all memberships belonging to the user with the given user ID.
    List<Membership> findByUserId(UUID userId);
    // Finds all memberships for the club with the given club ID.
    List<Membership> findByClubId(UUID clubId);
    // Finds the membership of a specific user in a specific club.
    Optional<Membership> findByUserIdAndClubId(UUID userId, UUID clubId);
    // Checks whether a specific user is already a member of a specific club.
    boolean existsByUserIdAndClubId(UUID userId, UUID clubId);
}
