package com.example.libraryapp.api.mapper;

import lombok.extern.slf4j.Slf4j;
import com.example.libraryapp.api.dto.BookDto;
import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.PublisherDto;
import com.example.libraryapp.domain.Author;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.domain.Genre;
import com.example.libraryapp.domain.Publisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookMapper {

    private final AuthorMapper authorMapper;
    private final GenreMapper genreMapper;
    private final ReviewMapper reviewMapper;

    public Book toEntity(BookDto dto) {
        Book book = new Book();
        book.setIsbn(dto.getIsbn());
        book.setTitle(dto.getTitle());
        book.setDescription(dto.getDescription());
        book.setPublicationYear(dto.getPublicationYear());
        book.setPrice(dto.getPrice());
        return book;
    }

    public BookResponseDto toDto(Book book) {
        if (book == null) {
            return null;
        }

        BookResponseDto dto = new BookResponseDto();
        dto.setId(book.getId());
        dto.setIsbn(book.getIsbn());
        dto.setTitle(book.getTitle());
        dto.setDescription(book.getDescription());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setPrice(book.getPrice());

        if (book.getPublisher() != null) {
            PublisherDto publisherDto = new PublisherDto();
            publisherDto.setId(book.getPublisher().getId());
            publisherDto.setName(book.getPublisher().getName());
            publisherDto.setAddress(book.getPublisher().getAddress());
            publisherDto.setPhone(book.getPublisher().getPhone());
            publisherDto.setEmail(book.getPublisher().getEmail());
            dto.setPublisher(publisherDto);
        }

        if (book.getAuthors() != null) {
            dto.setAuthors(book.getAuthors().stream()
                    .map(authorMapper::toDto)
                    .collect(Collectors.toSet()));
        }

        if (book.getGenres() != null) {
            dto.setGenres(book.getGenres().stream()
                    .map(genreMapper::toDto)
                    .collect(Collectors.toSet()));
        }

        if (book.getReviews() != null) {
            dto.setReviews(book.getReviews().stream()
                    .map(reviewMapper::toDto)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    /**
     * Конвертация Object[] из native query в Book
     * Порядок полей в SELECT:
     * 0: b.id
     * 1: b.isbn
     * 2: b.title
     * 3: b.description
     * 4: b.publication_year
     * 5: b.price
     * 6: b.average_rating
     * 7: a.id (author_id)
     * 8: a.name (author_name)
     * 9: g.id (genre_id)
     * 10: g.name (genre_name)
     * 11: p.id (publisher_id)
     * 12: p.name (publisher_name)
     */
    public Book mapToBook(Object[] row) {
        if (row == null || row.length < 13) {
            log.warn("Invalid row data: null or too short");
            return null;
        }

        Book book = new Book();

        try {
            int idx = 0;

            // id
            book.setId(convertToLong(row[idx++]));
            // isbn
            book.setIsbn(convertToString(row[idx++]));
            // title
            book.setTitle(convertToString(row[idx++]));
            // description
            book.setDescription(convertToString(row[idx++]));
            // publication_year
            book.setPublicationYear(convertToInteger(row[idx++]));
            // price
            book.setPrice(convertToBigDecimal(row[idx++]));
            // average_rating
            book.setAverageRating(convertToDouble(row[idx++]));

            // author_id
            Long authorId = convertToLong(row[idx++]);
            // author_name
            String authorName = convertToString(row[idx++]);
            if (authorId != null && authorName != null) {
                Author author = new Author();
                author.setId(authorId);
                author.setName(authorName);
                Set<Author> authors = new HashSet<>();
                authors.add(author);
                book.setAuthors(authors);
            }

            // genre_id
            Long genreId = convertToLong(row[idx++]);
            // genre_name
            String genreName = convertToString(row[idx++]);
            if (genreId != null && genreName != null) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(genreName);
                Set<Genre> genres = new HashSet<>();
                genres.add(genre);
                book.setGenres(genres);
            }

            // publisher_id
            Long publisherId = convertToLong(row[idx++]);
            // publisher_name
            String publisherName = convertToString(row[idx++]);
            if (publisherId != null && publisherName != null) {
                Publisher publisher = new Publisher();
                publisher.setId(publisherId);
                publisher.setName(publisherName);
                book.setPublisher(publisher);
            }

        } catch (Exception e) {
            log.error("Error mapping Object[] to Book: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to map database result to Book",
                    e);
        }

        return book;
    }

    // Вспомогательные методы для безопасного преобразования
    private Long convertToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException e) {
                log.warn("Cannot convert '{}' to Long", value);
                return null;
            }
        }
        return null;
    }

    private Integer convertToInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException e) {
                log.warn("Cannot convert '{}' to Integer", value);
                return null;
            }
        }
        return null;
    }

    private Double convertToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String string) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException e) {
                log.warn("Cannot convert '{}' to Double", value);
                return null;
            }
        }
        return null;
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String string) {
            try {
                return new BigDecimal(string);
            } catch (NumberFormatException e) {
                log.warn("Cannot convert '{}' to BigDecimal", value);
                return null;
            }
        }
        return null;
    }

    private String convertToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

}