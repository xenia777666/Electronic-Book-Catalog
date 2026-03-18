package com.example.libraryapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookSearchCriteria {
    private String authorName;
    private String genreName;
    private String publisherName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;

    // Составной ключ для индекса
    public String getCacheKey() {
        return String.format("%s|%s|%s|%s|%s|%s",
                nullSafeString(authorName),
                nullSafeString(genreName),
                nullSafeString(publisherName),
                nullSafeBigDecimal(minPrice),
                nullSafeBigDecimal(maxPrice),
                nullSafeDouble(minRating)
        );
    }

    private String nullSafeString(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    private String nullSafeBigDecimal(BigDecimal value) {
        return value == null ? "" : value.toString();
    }

    private String nullSafeDouble(Double value) {
        return value == null ? "" : value.toString();
    }
}