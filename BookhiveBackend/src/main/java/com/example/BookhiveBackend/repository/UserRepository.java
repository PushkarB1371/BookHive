package com.example.BookhiveBackend.repository;


import com.example.BookhiveBackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    // Finds a User by their email address.
    Optional<User> findByEmail(String email);
    // Checks whether a User with the given email address already exists.
    boolean existsByEmail(String email);
}
