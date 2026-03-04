package com.example.libraryapp.api.mapper;

import com.example.libraryapp.api.dto.*;
import com.example.libraryapp.domain.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookMapper {

    private final AuthorMapper authorMapper;
    private final GenreMapper genreMapper;
    private final PublisherMapper publisherMapper;
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
            dto.setPublisher(publisherMapper.toDto(book.getPublisher()));
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
}