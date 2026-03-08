package com.example.libraryapp.api.controller;

import com.example.libraryapp.api.dto.BookDto;
import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponseDto> createBook(@Valid @RequestBody BookDto bookDto) {
        log.info("POST /api/books - Creating new book: {}", bookDto.getTitle());
        BookResponseDto created = bookService.createBook(bookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDto>> getAllBooks(
            @PageableDefault(size = 10, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/books - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<BookResponseDto> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable Long id) {
        log.info("GET /api/books/{}", id);
        BookResponseDto book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDto> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookDto bookDto) {
        log.info("PUT /api/books/{} - Updating book", id);
        BookResponseDto updated = bookService.updateBook(id, bookDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.info("DELETE /api/books/{}", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/author")
    public ResponseEntity<List<BookResponseDto>> searchBooksByAuthor(
            @RequestParam String authorName) {
        log.info("GET /api/books/search/author?authorName={}", authorName);
        List<BookResponseDto> books = bookService.findBooksByAuthor(authorName);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/genre")
    public ResponseEntity<List<BookResponseDto>> searchBooksByGenre(
            @RequestParam String genreName) {
        log.info("GET /api/books/search/genre?genreName={}", genreName);
        List<BookResponseDto> books = bookService.findBooksByGenre(genreName);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/search/price")
    public ResponseEntity<List<BookResponseDto>> searchBooksByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        log.info("GET /api/books/search/price?minPrice={}&maxPrice={}", minPrice, maxPrice);
        List<BookResponseDto> books = bookService.findBooksByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/with-details")
    public ResponseEntity<List<BookResponseDto>> getAllBooksWithDetails() {
        log.info("GET /api/books/with-details - Loading books with authors, genres and publisher");
        List<BookResponseDto> books = bookService.getAllBooksWithDetails();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/with-n-plus-one")
    public ResponseEntity<List<BookResponseDto>> getAllBooksWithNPlusOneProblem() {
        log.info("GET /api/books/with-n-plus-one - DEMONSTRATING N+1 PROBLEM");
        List<BookResponseDto> books = bookService.getAllBooksWithNPlus1Problem();
        return ResponseEntity.ok(books);
    }

    @PostMapping("/without-transaction")
    public ResponseEntity<Book> createBookWithoutTransaction(@Valid @RequestBody BookDto bookDto) {
        log.info("POST /api/books/without-transaction - Demonstrating partial save");
        Book book = bookService.createBookWithoutTransaction(bookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }

    @PostMapping("/with-transaction")
    public ResponseEntity<Book> createBookWithTransaction(@Valid @RequestBody BookDto bookDto) {
        log.info("POST /api/books/with-transaction - Demonstrating atomic save");
        Book book = bookService.createBookWithTransaction(bookDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(book);
    }
}