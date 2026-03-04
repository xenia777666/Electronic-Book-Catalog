package com.example.libraryapp.api.mapper;

import com.example.libraryapp.api.dto.ReviewDto;
import com.example.libraryapp.domain.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDto toDto(Review review) {
        if (review == null) {
            return null;
        }

        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setReviewerName(review.getReviewerName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setBookId(review.getBook() != null ? review.getBook().getId() : null);
        return dto;
    }

    public Review toEntity(ReviewDto dto) {
        if (dto == null) {
            return null;
        }

        Review review = new Review();
        review.setId(dto.getId());
        review.setReviewerName(dto.getReviewerName());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        return review;
    }
}