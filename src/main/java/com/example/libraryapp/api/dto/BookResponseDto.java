package com.example.libraryapp.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookResponseDto {
    private Long id;
    private String title;
    private String author;
    private Integer publicationYear;
}