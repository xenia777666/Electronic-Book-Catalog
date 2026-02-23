package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.mapper.BookMapper;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.repository.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    public List<BookResponseDto> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public BookResponseDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return bookMapper.toDto(book);
    }

    public List<BookResponseDto> getBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author).stream()
                .map(bookMapper::toDto)
                .toList();
    }
}