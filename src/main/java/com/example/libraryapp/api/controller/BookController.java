package com.example.libraryapp.api.controller;

import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable Long id) {
        BookResponseDto book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDto>> getBooks(
            @RequestParam(required = false) String author) {
        if (author != null && !author.isBlank()) {
            return ResponseEntity.ok(bookService.getBooksByAuthor(author));
        }
        return ResponseEntity.ok(bookService.getAllBooks());
    }
}