package com.example.libraryapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateResultDto {
    private int totalRequested;
    private int successful;
    private int failed;
    private List<BookResponseDto> createdBooks = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
}