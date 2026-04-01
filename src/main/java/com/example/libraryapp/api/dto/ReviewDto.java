package com.example.libraryapp.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewDto {
    private Long id;

    @Size(min = 2, max = 100, message = "Имя reviewer должно быть от 2 до 100 символов")
    private String reviewerName;

    @NotNull(message = "Рейтинг обязателен")
    @Min(value = 1, message = "Рейтинг должен быть от 1 до 5")
    @Max(value = 5, message = "Рейтинг должен быть от 1 до 5")
    private Integer rating;

    @Size(max = 2000, message = "Комментарий не может превышать 2000 символов")
    private String comment;

    @NotNull(message = "ID книги обязателен")
    private Long bookId;
}