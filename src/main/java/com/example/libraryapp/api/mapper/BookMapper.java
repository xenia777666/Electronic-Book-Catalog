package com.example.libraryapp.api.mapper;

import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.domain.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookResponseDto toDto(Book book) {
        if (book == null) {
            return null;
        }

        BookResponseDto dto = new BookResponseDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublicationYear(book.getPublicationYear());
        return dto;
    }
}