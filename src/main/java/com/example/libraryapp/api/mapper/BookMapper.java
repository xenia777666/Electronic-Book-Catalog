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

    public Book mapToBook(Object[] row) {
        if (row == null || row.length < 13) {
            log.warn("Invalid row data: null or too short");
            return null;
        }

        Book book = new Book();

        try {
            int idx = 0;

            Long id = convertToLong(row[idx]);
            idx++;
            book.setId(id);

            String isbn = convertToString(row[idx]);
            idx++;
            book.setIsbn(isbn);

            String title = convertToString(row[idx]);
            idx++;
            book.setTitle(title);

            String description = convertToString(row[idx]);
            idx++;
            book.setDescription(description);

            Integer publicationYear = convertToInteger(row[idx]);
            idx++;
            book.setPublicationYear(publicationYear);

            BigDecimal price = convertToBigDecimal(row[idx]);
            idx++;
            book.setPrice(price);

            Double averageRating = convertToDouble(row[idx]);
            idx++;
            book.setAverageRating(averageRating);

            Long authorId = convertToLong(row[idx]);
            idx++;
            String authorName = convertToString(row[idx]);
            idx++;

            if (authorId != null && authorName != null) {
                Author author = new Author();
                author.setId(authorId);
                author.setName(authorName);
                Set<Author> authors = new HashSet<>();
                authors.add(author);
                book.setAuthors(authors);
            }

            Long genreId = convertToLong(row[idx]);
            idx++;
            String genreName = convertToString(row[idx]);
            idx++;

            if (genreId != null && genreName != null) {
                Genre genre = new Genre();
                genre.setId(genreId);
                genre.setName(genreName);
                Set<Genre> genres = new HashSet<>();
                genres.add(genre);
                book.setGenres(genres);
            }

            Long publisherId = convertToLong(row[idx]);
            idx++;
            String publisherName = convertToString(row[idx]);


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