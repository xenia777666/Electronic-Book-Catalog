package com.example.libraryapp.repository;

import com.example.libraryapp.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByBookId(Long bookId);

    List<Review> findByRating(Integer rating);
}