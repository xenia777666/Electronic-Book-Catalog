package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.ReviewDto;
import com.example.libraryapp.api.mapper.ReviewMapper;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.domain.Review;
import com.example.libraryapp.repository.BookRepository;
import com.example.libraryapp.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto) {
        log.info("Creating review for book id: {}", reviewDto.getBookId());

        Book book = bookRepository.findById(reviewDto.getBookId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Book not found with id: " + reviewDto.getBookId()));

        Review review = reviewMapper.toEntity(reviewDto);
        review.setBook(book);

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    public List<ReviewDto> getAllReviews() {
        log.debug("Getting all reviews");
        return reviewRepository.findAll()
                .stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public ReviewDto getReviewById(Long id) {
        log.debug("Getting review by id: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Review not found with id: " + id));
        return reviewMapper.toDto(review);
    }

    public List<ReviewDto> getReviewsByBookId(Long bookId) {
        log.debug("Getting reviews for book id: {}", bookId);
        return reviewRepository.findByBookId(bookId)
                .stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto updateReview(Long id, ReviewDto reviewDto) {
        log.info("Updating review with id: {}", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Review not found with id: " + id));

        if (reviewDto.getReviewerName() != null) {
            review.setReviewerName(reviewDto.getReviewerName());
        }
        if (reviewDto.getRating() != null) {
            review.setRating(reviewDto.getRating());
        }
        if (reviewDto.getComment() != null) {
            review.setComment(reviewDto.getComment());
        }

        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toDto(updatedReview);
    }

    @Transactional
    public void deleteReview(Long id) {
        log.info("Deleting review with id: {}", id);

        if (!reviewRepository.existsById(id)) {
            throw new EntityNotFoundException("Review not found with id: " + id);
        }
        reviewRepository.deleteById(id);
    }
}