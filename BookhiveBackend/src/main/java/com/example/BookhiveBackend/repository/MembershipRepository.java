package com.example.BookhiveBackend.repository;

import com.example.BookhiveBackend.entity.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
    List<Membership> findByUserId(UUID userId);
    List<Membership> findByClubId(UUID clubId);
    Optional<Membership> findByUserIdAndClubId(UUID userId, UUID clubId);
    boolean existsByUserIdAndClubId(UUID userId, UUID clubId);
    void deleteByClubId(UUID clubId);
}