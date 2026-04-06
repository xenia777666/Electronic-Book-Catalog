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

    @Test
    void updateBook_Success() {
        BookDto updateDto = new BookDto();
        updateDto.setIsbn("978-3-16-148410-1");
        updateDto.setTitle("Updated Book");
        updateDto.setPublisherId(1L);
        updateDto.setAuthorIds(Set.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn(updateDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(any())).thenReturn(List.of(author));
        when(genreRepository.findAllById(any())).thenReturn(List.of(genre));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.updateBook(1L, updateDto);

        assertThat(result.getTitle()).isEqualTo("Test Book");
        verify(bookRepository).save(book);
        verify(indexService).invalidateCache();
    }

    @Test
    void updateBook_IsbnExists_ThrowsConflict() {
        BookDto updateDto = new BookDto();
        updateDto.setIsbn("978-3-16-148410-1");
        updateDto.setTitle("Updated Book");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn(updateDto.getIsbn())).thenReturn(Optional.of(new Book()));

        assertThatThrownBy(() -> bookService.updateBook(1L, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void updateBook_NotFound_ThrowsNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(999L, bookDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void bulkCreateBooks_Success() {
        List<BookDto> books = List.of(bookDto);

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(any())).thenReturn(List.of(author));
        when(genreRepository.findAllById(any())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(bookRepository.saveAll(anyList())).thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BulkCreateResultDto result = bookService.bulkCreateBooks(books);

        assertThat(result.getSuccessful()).isEqualTo(1);
    }

    @Test
    void bulkCreateBooksWithTransaction_Success() {
        List<BookDto> books = List.of(bookDto);

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(any())).thenReturn(List.of(author));
        when(genreRepository.findAllById(any())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(bookRepository.saveAll(anyList())).thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BulkCreateResultDto result = bookService.bulkCreateBooksWithTransaction(books);

        assertThat(result.getSuccessful()).isEqualTo(1);
    }

    @Test
    void bulkCreateBooksWithTransaction_Duplicate_ThrowsException() {
        List<BookDto> books = List.of(bookDto, bookDto);

        when(bookRepository.findByIsbn(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.bulkCreateBooksWithTransaction(books))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void searchBooks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAuthorName("Test");

        when(bookRepository.findBooksByComplexCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.searchBooks(criteria, pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchBooksWithPagination_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchCriteria criteria = new BookSearchCriteria();

        when(bookRepository.findBooksByComplexCriteriaWithPagination(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        Page<BookResponseDto> result = bookService.searchBooksWithPagination(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findBooksByAuthor_Success() {
        when(bookRepository.findBooksByComplexCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.findBooksByAuthor("Test");

        assertThat(result).hasSize(1);
    }

    @Test
    void findBooksByGenre_Success() {
        when(bookRepository.findBooksByComplexCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.findBooksByGenre("Test");

        assertThat(result).hasSize(1);
    }

    @Test
    void findBooksByPriceRange_Success() {
        when(bookRepository.findBooksByComplexCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.findBooksByPriceRange(BigDecimal.TEN, BigDecimal.valueOf(100));

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllBooksWithDetails_Success() {
        when(bookRepository.findAllWithDetails()).thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.getAllBooksWithDetails();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllBooksWithNPlus1Problem_Success() {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        List<BookResponseDto> result = bookService.getAllBooksWithNPlus1Problem();

        assertThat(result).hasSize(1);
    }

    @Test
    void createBookWithoutTransaction_Success() {
        BookDto dto = new BookDto();
        dto.setTitle("Test Book");
        dto.setIsbn("978-3-16-148410-0");

        when(publisherRepository.save(any())).thenReturn(publisher);
        when(authorRepository.save(any())).thenReturn(author);
        when(bookMapper.toEntity(dto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.createBookWithoutTransaction(dto);

        assertThat(result).isNotNull();
    }

    @Test
    void createBookWithTransaction_Success() {
        BookDto dto = new BookDto();
        dto.setTitle("Test Book");
        dto.setIsbn("978-3-16-148410-0");

        when(publisherRepository.save(any())).thenReturn(publisher);
        when(authorRepository.save(any())).thenReturn(author);
        when(bookMapper.toEntity(dto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.createBookWithTransaction(dto);

        assertThat(result).isNotNull();
    }
}