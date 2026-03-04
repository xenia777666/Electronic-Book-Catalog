package com.example.libraryapp.api.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class AuthorDto {
    private Long id;
    private String name;
    private String biography;
    private LocalDate birthDate;
    private String nationality;
}