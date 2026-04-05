package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.*;
import com.example.libraryapp.api.mapper.BookMapper;
import com.example.libraryapp.domain.*;
import com.example.libraryapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private PublisherRepository publisherRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private BookMapper bookMapper;
    @Mock private IndexService indexService;  // Мок для IndexService

    private BookService bookService;  // ← Ручное создание

    private BookDto bookDto;
    private Book book;
    private BookResponseDto bookResponseDto;
    private Publisher publisher;
    private Author author;
    private Genre genre;

    @BeforeEach
    void setUp() {
        // ← РУЧНОЕ СОЗДАНИЕ BookService
        bookService = new BookService(
                bookRepository, authorRepository, publisherRepository,
                genreRepository, bookMapper, indexService
        );

        bookDto = new BookDto();
        bookDto.setIsbn("978-3-16-148410-0");
        bookDto.setTitle("Test Book");
        bookDto.setPublisherId(1L);
        bookDto.setAuthorIds(Set.of(1L));
        bookDto.setGenreIds(Set.of(1L));

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");

        bookResponseDto = new BookResponseDto();
        bookResponseDto.setId(1L);
        bookResponseDto.setTitle("Test Book");

        publisher = new Publisher();
        publisher.setId(1L);

        author = new Author();
        author.setId(1L);

        genre = new Genre();
        genre.setId(1L);
    }

    @Test
    void createBook_Success() {
        // given
        when(bookRepository.findByIsbn(bookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        // when
        BookResponseDto result = bookService.createBook(bookDto);

        // then
        assertThat(result.getTitle()).isEqualTo("Test Book");
        verify(bookRepository).save(book);
        verify(indexService).invalidateCache();
    }

    @Test
    void createBook_IsbnExists_ThrowsConflict() {
        when(bookRepository.findByIsbn(bookDto.getIsbn())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.createBook(bookDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void getBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.getBookById(1L);

        assertThat(result.getTitle()).isEqualTo("Test Book");
    }

    @Test
    void getBookById_NotFound_ThrowsNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteBook_Success() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        assertThatNoException().isThrownBy(() -> bookService.deleteBook(1L));

        verify(bookRepository).deleteById(1L);
        verify(indexService).invalidateCache();
    }

    @Test
    void deleteBook_NotFound_ThrowsNotFound() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void bulkCreateBooksWithoutTransaction_Success() {
        List<BookDto> books = List.of(bookDto);

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(any())).thenReturn(List.of(author));
        when(genreRepository.findAllById(any())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BulkCreateResultDto result = bookService.bulkCreateBooksWithoutTransaction(books);

        assertThat(result.getSuccessful()).isEqualTo(1);
        assertThat(result.getFailed()).isZero();
    }

    @Test
    void getAllBooks_Pagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(page);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        Page<BookResponseDto> result = bookService.getAllBooks(pageable);

        assertThat(result.getContent()).hasSize(1);
    }
}