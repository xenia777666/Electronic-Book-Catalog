package com.example.libraryapp.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PublisherDto {
    private Long id;

    @NotBlank(message = "Название издателя обязательно")
    @Size(min = 2, max = 255, message = "Название должно быть от 2 до 255 символов")
    private String name;

    private String address;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{10,20}$", message = "Неверный формат телефона")
    private String phone;

    @Email(message = "Неверный формат email")
    private String email;
}