package com.example.BookhiveBackend.repository;


import com.example.BookhiveBackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByBookId(UUID bookId);
}
