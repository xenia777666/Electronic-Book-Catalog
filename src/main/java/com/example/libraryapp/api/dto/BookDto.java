package com.example.libraryapp.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class BookDto {

    @NotBlank(message = "ISBN обязателен")
    @Pattern(regexp = "^(97[8-9])?\\d{9}[\\dX]$", message = "Неверный формат ISBN")
    private String isbn;

    @NotBlank(message = "Название обязательно")
    @Size(min = 2, max = 255, message = "Название должно быть от 2 до 255 символов")
    private String title;

    private String description;

    @Min(value = 1455, message = "Год не может быть раньше 1455")
    @Max(value = 2026, message = "Год не может быть в будущем")
    private Integer publicationYear;

    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private BigDecimal price;

    @NotNull(message = "ID издателя обязателен")
    private Long publisherId;

    @Size(min = 1, message = "Должен быть хотя бы один автор")
    private Set<Long> authorIds;

    private Set<Long> genreIds;
}