package com.example.libraryapp.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class BookResponseDto {
    private Long id;
    private String isbn;
    private String title;
    private String description;
    private Integer publicationYear;
    private BigDecimal price;

    private PublisherDto publisher;
    private Set<AuthorDto> authors;
    private Set<GenreDto> genres;
    private Set<ReviewDto> reviews;
}