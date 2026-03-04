package com.example.libraryapp.api.dto;

import lombok.Data;

@Data
public class ReviewDto {
    private Long id;
    private String reviewerName;
    private Integer rating;
    private String comment;
    private Long bookId;
}