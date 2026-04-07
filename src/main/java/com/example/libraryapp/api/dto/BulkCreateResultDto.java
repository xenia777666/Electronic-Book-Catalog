package com.example.libraryapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateResultDto {
    private List<BookResult> results;
    private int totalSuccess;
    private int totalFailed;
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookResult {
        private String isbn;
        private boolean success;
        private String message;
        private String errorMessage;
    }
}