package com.example.libraryapp.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchCriteria {
    private String authorName;
    private String genreName;
    private String publisherName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;

    // Для отладки
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

    // ============= ПРАВИЛЬНАЯ РЕАЛИЗАЦИЯ equals() =============
    @SuppressWarnings("checkstyle:NeedBraces")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BookSearchCriteria that = (BookSearchCriteria) o;
        return Objects.equals(authorName, that.authorName)
                && Objects.equals(genreName, that.genreName)
                && Objects.equals(publisherName, that.publisherName)
                && Objects.equals(minPrice, that.minPrice)
                && Objects.equals(maxPrice, that.maxPrice)
                && Objects.equals(minRating, that.minRating);
    }

    // ============= ПРАВИЛЬНАЯ РЕАЛИЗАЦИЯ hashCode() =============
    @Override
    public int hashCode() {
        return Objects.hash(
                authorName,
                genreName,
                publisherName,
                minPrice,
                maxPrice,
                minRating
        );
    }
}