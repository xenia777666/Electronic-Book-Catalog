package com.example.libraryapp.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AuthorDto {
    private Long id;

    @NotBlank(message = "Имя автора обязательно")
    @Size(min = 2, max = 255, message = "Имя должно быть от 2 до 255 символов")
    private String name;

    private String biography;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthDate;
}