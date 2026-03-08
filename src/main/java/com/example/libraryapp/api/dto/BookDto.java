package com.example.libraryapp.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class BookDto {

    private String isbn;

    private String title;

    private String description;

    private Integer publicationYear;

    private BigDecimal price;

    private Long publisherId;

    private Set<Long> authorIds;

    private Set<Long> genreIds;
}