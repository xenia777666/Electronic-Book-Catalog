package com.example.libraryapp.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.example.libraryapp.api.dto.*;
import com.example.libraryapp.api.mapper.BookMapper;
import com.example.libraryapp.domain.*;
import com.example.libraryapp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;



import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private PublisherRepository publisherRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private IndexService indexService;

    private BookService bookService;

    private BookDto bookDto;
    private Book book;
    private BookResponseDto bookResponseDto;
    private Publisher publisher;
    private Author author;
    private Genre genre;

    @BeforeEach
    void setUp() {
        bookService = new BookService(
                bookRepository, authorRepository, publisherRepository,
                genreRepository, bookMapper, indexService
        );

        bookDto = new BookDto();
        bookDto.setIsbn("978-3-16-148410-0");
        bookDto.setTitle("Test Book");
        bookDto.setPrice(new BigDecimal("99.99"));
        bookDto.setPublisherId(1L);
        bookDto.setAuthorIds(Set.of(1L));
        bookDto.setGenreIds(Set.of(1L));

        book = new Book();
        book.setId(1L);
        book.setIsbn("978-3-16-148410-0");
        book.setTitle("Test Book");

        bookResponseDto = new BookResponseDto();
        bookResponseDto.setId(1L);
        bookResponseDto.setTitle("Test Book");

        publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Test Publisher");

        author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Test Genre");
    }

    // ============= CREATE =============

    @Test
    void createBook_Success() {
        when(bookRepository.findByIsbn(bookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(genreRepository.findAllById(anySet())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.createBook(bookDto);

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
    void createBook_DataIntegrityViolation_ThrowsConflict() {
        when(bookRepository.findByIsbn(bookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(genreRepository.findAllById(anySet())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(bookRepository.save(book)).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> bookService.createBook(bookDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void createBook_AuthorsNotFound_ThrowsNotFound() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-1");
        dto.setTitle("Test Book");
        dto.setPublisherId(1L);
        dto.setAuthorIds(Set.of(999L));

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(book);  // ← ДОБАВИТЬ!
        when(authorRepository.findAllById(anySet())).thenReturn(List.of());

        assertThatThrownBy(() -> bookService.createBook(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    @Test
    void createBook_GenresNotFound_ThrowsNotFound() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-1");
        dto.setTitle("Test Book");
        dto.setPublisherId(1L);
        dto.setAuthorIds(Set.of(1L));
        dto.setGenreIds(Set.of(999L));

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(book);  // ← ДОБАВИТЬ!
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(genreRepository.findAllById(anySet())).thenReturn(List.of());

        assertThatThrownBy(() -> bookService.createBook(dto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND);
    }

    // ============= READ =============

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
    void getAllBooks_Pagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(book));

        when(bookRepository.findAll(pageable)).thenReturn(page);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        Page<BookResponseDto> result = bookService.getAllBooks(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Book");
    }

    // ============= UPDATE =============

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
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
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

        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setIsbn("978-3-16-148410-0");
        existingBook.setTitle("Original Book");

        Book duplicateBook = new Book();
        duplicateBook.setId(2L);
        duplicateBook.setIsbn("978-3-16-148410-1");
        duplicateBook.setTitle("Duplicate Book");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn(updateDto.getIsbn())).thenReturn(Optional.of(duplicateBook));

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
    void updateBook_DataIntegrityViolation_ThrowsConflict() {
        BookDto updateDto = new BookDto();
        updateDto.setIsbn("978-3-16-148410-1");
        updateDto.setTitle("Updated Book");
        updateDto.setPublisherId(1L);
        updateDto.setAuthorIds(Set.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn(updateDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(bookRepository.save(book)).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> bookService.updateBook(1L, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    // ============= DELETE =============

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

    // ============= BULK OPERATIONS =============

    @Test
    void bulkCreateBooks_Success() {
        List<BookDto> books = List.of(bookDto);

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(genreRepository.findAllById(anySet())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(book);
        when(bookRepository.saveAll(anyList())).thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BulkCreateResultDto result = bookService.bulkCreateBooks(books);

        assertThat(result.getSuccessful()).isEqualTo(1);
        assertThat(result.getFailed()).isZero();
    }

    @Test
    void bulkCreateBooks_DuplicateIsbn_ReturnsEmpty() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-0");
        dto.setTitle("Test Book");

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

        List<BookDto> books = List.of(dto);
        BulkCreateResultDto result = bookService.bulkCreateBooks(books);

        assertThat(result.getSuccessful()).isZero();
        assertThat(result.getFailed()).isEqualTo(1);
    }


    @Test
    void bulkCreateBooks_PrepareEntityException_ReturnsEmpty() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-0");
        dto.setTitle("Test Book");

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(dto)).thenThrow(new RuntimeException("Mapping error"));

        List<BookDto> books = List.of(dto);
        BulkCreateResultDto result = bookService.bulkCreateBooks(books);

        assertThat(result.getSuccessful()).isZero();
        assertThat(result.getFailed()).isEqualTo(1);
    }

    @Test
    void bulkCreateBooksWithoutTransaction_Success() {
        List<BookDto> books = List.of(bookDto);

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(genreRepository.findAllById(anySet())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(bookDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BulkCreateResultDto result = bookService.bulkCreateBooksWithoutTransaction(books);

        assertThat(result.getSuccessful()).isEqualTo(1);
        assertThat(result.getFailed()).isZero();
    }

    @Test
    void bulkCreateBooksWithoutTransaction_DuplicateIsbn_AddsError() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-0");
        dto.setTitle("Test Book");

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

        List<BookDto> books = List.of(dto);
        BulkCreateResultDto result = bookService.bulkCreateBooksWithoutTransaction(books);

        assertThat(result.getSuccessful()).isZero();
        assertThat(result.getFailed()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    void bulkCreateBooksWithoutTransaction_Exception_AddsError() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-0");
        dto.setTitle("Test Book");

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(genreRepository.findAllById(anySet())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(dto)).thenReturn(book);
        when(bookRepository.save(book)).thenThrow(new RuntimeException("DB error"));

        List<BookDto> books = List.of(dto);
        BulkCreateResultDto result = bookService.bulkCreateBooksWithoutTransaction(books);

        assertThat(result.getSuccessful()).isZero();
        assertThat(result.getFailed()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    void bulkCreateBooksWithTransaction_Success() {
        List<BookDto> books = List.of(bookDto);

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(genreRepository.findAllById(anySet())).thenReturn(List.of(genre));
        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(book);
        when(bookRepository.saveAll(anyList())).thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BulkCreateResultDto result = bookService.bulkCreateBooksWithTransaction(books);

        assertThat(result.getSuccessful()).isEqualTo(1);
        assertThat(result.getFailed()).isZero();
    }

    @Test
    void bulkCreateBooksWithTransaction_DuplicateIsbn_ThrowsException() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-0");
        dto.setTitle("Test Book");

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(book));

        List<BookDto> books = List.of(dto);
        assertThatThrownBy(() -> bookService.bulkCreateBooksWithTransaction(books))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    // ============= SEARCH METHODS =============

    @Test
    void searchBooks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAuthorName("Test");

        when(indexService.getFromCache(any(), any())).thenReturn(null);
        when(bookRepository.findBooksByComplexCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);
        doNothing().when(indexService).putInCache(any(), any(), any());

        List<BookResponseDto> result = bookService.searchBooks(criteria, pageable);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchBooks_CacheHit() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAuthorName("Test");

        when(indexService.getFromCache(any(), any())).thenReturn(List.of(bookResponseDto));

        List<BookResponseDto> result = bookService.searchBooks(criteria, pageable);

        assertThat(result).hasSize(1);
        verify(bookRepository, never()).findBooksByComplexCriteria(any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchBooksWithPagination_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchCriteria criteria = new BookSearchCriteria();

        when(indexService.getPageFromCache(any(), any())).thenReturn(null);
        when(bookRepository.findBooksByComplexCriteriaWithPagination(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);
        doNothing().when(indexService).putPageInCache(any(), any(), any());

        Page<BookResponseDto> result = bookService.searchBooksWithPagination(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchBooksWithPagination_CacheHit() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchCriteria criteria = new BookSearchCriteria();

        Page<BookResponseDto> cachedPage = new PageImpl<>(List.of(bookResponseDto));
        when(indexService.getPageFromCache(any(), any())).thenReturn(cachedPage);

        Page<BookResponseDto> result = bookService.searchBooksWithPagination(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository, never()).findBooksByComplexCriteriaWithPagination(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void findBooksByAuthor_Success() {
        when(indexService.getFromCache(any(), any())).thenReturn(null);
        when(bookRepository.findBooksByComplexCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);
        doNothing().when(indexService).putInCache(any(), any(), any());

        List<BookResponseDto> result = bookService.findBooksByAuthor("Test");

        assertThat(result).hasSize(1);
    }

    @Test
    void findBooksByAuthor_CacheHit() {
        when(indexService.getFromCache(any(), any())).thenReturn(List.of(bookResponseDto));

        List<BookResponseDto> result = bookService.findBooksByAuthor("Test");

        assertThat(result).hasSize(1);
        verify(bookRepository, never()).findBooksByComplexCriteria(any(), any(), any(), any(), any(), any());
    }

    @Test
    void findBooksByGenre_Success() {
        when(indexService.getFromCache(any(), any())).thenReturn(null);
        when(bookRepository.findBooksByComplexCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);
        doNothing().when(indexService).putInCache(any(), any(), any());

        List<BookResponseDto> result = bookService.findBooksByGenre("Test");

        assertThat(result).hasSize(1);
    }

    @Test
    void findBooksByGenre_CacheHit() {
        when(indexService.getFromCache(any(), any())).thenReturn(List.of(bookResponseDto));

        List<BookResponseDto> result = bookService.findBooksByGenre("Test");

        assertThat(result).hasSize(1);
        verify(bookRepository, never()).findBooksByComplexCriteria(any(), any(), any(), any(), any(), any());
    }

    @Test
    void findBooksByPriceRange_Success() {
        when(indexService.getFromCache(any(), any())).thenReturn(null);
        when(bookRepository.findBooksByComplexCriteria(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);
        doNothing().when(indexService).putInCache(any(), any(), any());

        List<BookResponseDto> result = bookService.findBooksByPriceRange(BigDecimal.TEN, BigDecimal.valueOf(100));

        assertThat(result).hasSize(1);
    }

    @Test
    void findBooksByPriceRange_CacheHit() {
        when(indexService.getFromCache(any(), any())).thenReturn(List.of(bookResponseDto));

        List<BookResponseDto> result = bookService.findBooksByPriceRange(BigDecimal.TEN, BigDecimal.valueOf(100));

        assertThat(result).hasSize(1);
        verify(bookRepository, never()).findBooksByComplexCriteria(any(), any(), any(), any(), any(), any());
    }

    // ============= NATIVE METHODS =============

    @Test
    void searchBooksNative_Success() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAuthorName("Test");

        Object[] row = new Object[13];
        row[0] = 1L;
        row[1] = "978-3-16-148410-0";
        row[2] = "Test Book";
        row[3] = "Description";
        row[4] = 2024;
        row[5] = new BigDecimal("99.99");
        row[6] = 4.5;
        row[7] = 1L;
        row[8] = "Test Author";
        row[9] = 1L;
        row[10] = "Test Genre";
        row[11] = 1L;
        row[12] = "Test Publisher";

        when(indexService.getFromCache(any(), any())).thenReturn(null);
        when(bookRepository.findBooksByComplexCriteriaNative(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row));
        when(bookMapper.mapToBook(any())).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);
        doNothing().when(indexService).putInCache(any(), any(), any());

        List<BookResponseDto> result = bookService.searchBooksNative(criteria);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Book");
    }

    @Test
    void searchBooksNative_CacheHit() {
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAuthorName("Test");

        when(indexService.getFromCache(any(), any())).thenReturn(List.of(bookResponseDto));

        List<BookResponseDto> result = bookService.searchBooksNative(criteria);

        assertThat(result).hasSize(1);
        verify(bookRepository, never()).findBooksByComplexCriteriaNative(any(), any(), any(), any(), any(), any());
    }

    @Test
    void searchBooksNativeWithPagination_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        BookSearchCriteria criteria = new BookSearchCriteria();

        Object[] row = new Object[13];
        row[0] = 1L;
        row[1] = "978-3-16-148410-0";
        row[2] = "Test Book";
        row[3] = "Description";
        row[4] = 2024;
        row[5] = new BigDecimal("99.99");
        row[6] = 4.5;
        row[7] = 1L;
        row[8] = "Test Author";
        row[9] = 1L;
        row[10] = "Test Genre";
        row[11] = 1L;
        row[12] = "Test Publisher";

        Page<Object[]> page = new PageImpl<>(Collections.singletonList(row), pageable, 1);

        when(bookRepository.findBooksByComplexCriteriaNativeWithPagination(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(page);
        when(bookMapper.mapToBook(any())).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        Page<BookResponseDto> result = bookService.searchBooksNativeWithPagination(criteria, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Book");
    }

    // ============= DEMONSTRATION METHODS =============

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
        assertThat(result.getTitle()).isEqualTo("Test Book");
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
        assertThat(result.getTitle()).isEqualTo("Test Book");
    }

    // ============= ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ 100% ПОКРЫТИЯ =============

    @Test
    void updateBook_WithSameIsbn_DoesNotCheckDuplicate() {
        BookDto updateDto = new BookDto();
        updateDto.setIsbn("978-3-16-148410-0");  // тот же ISBN что и у книги
        updateDto.setTitle("Updated Book");
        updateDto.setPublisherId(1L);
        updateDto.setAuthorIds(Set.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        // Не должно быть вызова findByIsbn, так как ISBN не изменился
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.updateBook(1L, updateDto);

        assertThat(result.getTitle()).isEqualTo("Test Book");
        verify(bookRepository, never()).findByIsbn(anyString());
    }

    @Test
    void createBookWithTempEntities_WithError_ThrowsException_WithTransaction() {
        BookDto dto = new BookDto();
        dto.setTitle("error test");
        dto.setIsbn("978-3-16-148410-0");

        when(publisherRepository.save(any())).thenReturn(publisher);
        when(authorRepository.save(any())).thenReturn(author);
        when(bookMapper.toEntity(dto)).thenReturn(book);

        assertThatThrownBy(() -> bookService.createBookWithTransaction(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Simulating error during save - transaction will rollback!");
    }

    @Test
    void setBookGenres_WithNullGenreIds_Skip() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-1");
        dto.setTitle("Test Book");
        dto.setPublisherId(1L);
        dto.setAuthorIds(Set.of(1L));
        dto.setGenreIds(null);  // null жанры

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.createBook(dto);

        assertThat(result).isNotNull();
        verify(genreRepository, never()).findAllById(anySet());
    }

    @Test
    void setBookGenres_WithEmptyGenreIds_Skip() {
        BookDto dto = new BookDto();
        dto.setIsbn("978-3-16-148410-1");
        dto.setTitle("Test Book");
        dto.setPublisherId(1L);
        dto.setAuthorIds(Set.of(1L));
        dto.setGenreIds(Set.of());  // пустой Set

        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(anySet())).thenReturn(List.of(author));
        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.createBook(dto);

        assertThat(result).isNotNull();
        verify(genreRepository, never()).findAllById(anySet());
    }

    @Test
    void createBookWithoutTransaction_ErrorTitle_ThrowsException() {
        BookDto dto = new BookDto();
        dto.setTitle("error test");
        dto.setIsbn("978-3-16-148410-0");

        when(publisherRepository.save(any())).thenReturn(publisher);
        when(authorRepository.save(any())).thenReturn(author);
        when(bookMapper.toEntity(dto)).thenReturn(book);

        assertThatThrownBy(() -> bookService.createBookWithoutTransaction(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Simulating error during save!");
    }


    // ← ЭТОТ ТЕСТ МОЖЕТ НЕ ХВАТАТЬ!
    @Test
    void createBookWithTransaction_NormalTitle_NoError() {
        BookDto dto = new BookDto();
        dto.setTitle("normal title");  // НЕТ слова "error"
        dto.setIsbn("978-3-16-148410-0");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setPublisherId(1L);
        dto.setAuthorIds(Set.of(1L));

        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher);
        when(authorRepository.save(any(Author.class))).thenReturn(author);
        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(book);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.createBookWithTransaction(dto);

        assertThat(result).isNotNull();
    }

    @Test
    void createBookWithTransaction_NullTitle_NoError() {
        BookDto dto = new BookDto();
        dto.setTitle(null);  // ← NULL
        dto.setIsbn("978-3-16-148410-0");
        dto.setPrice(new BigDecimal("99.99"));
        dto.setPublisherId(1L);
        dto.setAuthorIds(Set.of(1L));

        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher);
        when(authorRepository.save(any(Author.class))).thenReturn(author);
        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(book);
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.createBookWithTransaction(dto);

        assertThat(result).isNotNull();
    }
}