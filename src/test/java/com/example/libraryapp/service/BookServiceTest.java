package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookDto;
import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.BookSearchCriteria;
import com.example.libraryapp.api.dto.BulkCreateResultDto;
import com.example.libraryapp.api.mapper.BookMapper;
import com.example.libraryapp.domain.Author;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.domain.Genre;
import com.example.libraryapp.domain.Publisher;
import com.example.libraryapp.repository.AuthorRepository;
import com.example.libraryapp.repository.BookRepository;
import com.example.libraryapp.repository.GenreRepository;
import com.example.libraryapp.repository.PublisherRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    private BookDto validBookDto;
    private BookDto anotherValidBookDto;
    private Book book;
    private Book anotherBook;
    private Book savedBook;
    private BookResponseDto bookResponseDto;
    private Publisher publisher;
    private Author author;
    private Genre genre;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Test Publisher");

        author = new Author();
        author.setId(1L);
        author.setName("Test Author");

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Test Genre");

        validBookDto = new BookDto();
        validBookDto.setIsbn("9785043333333");
        validBookDto.setTitle("Первая книга");
        validBookDto.setDescription("Description");
        validBookDto.setPublicationYear(2024);
        validBookDto.setPrice(new BigDecimal("500"));
        validBookDto.setPublisherId(1L);
        validBookDto.setAuthorIds(Set.of(1L));

        anotherValidBookDto = new BookDto();
        anotherValidBookDto.setIsbn("9785699180312");
        anotherValidBookDto.setTitle("Вторая книга");
        anotherValidBookDto.setDescription("Description");
        anotherValidBookDto.setPublicationYear(2024);
        anotherValidBookDto.setPrice(new BigDecimal("600"));
        anotherValidBookDto.setPublisherId(1L);
        anotherValidBookDto.setAuthorIds(Set.of(1L));

        book = new Book();
        book.setId(1L);
        book.setIsbn("9785043333333");
        book.setTitle("Первая книга");
        book.setDescription("Description");
        book.setPublicationYear(2024);
        book.setPrice(new BigDecimal("500"));
        book.setPublisher(publisher);
        book.setAuthors(Set.of(author));

        anotherBook = new Book();
        anotherBook.setId(2L);
        anotherBook.setIsbn("9785699180312");
        anotherBook.setTitle("Вторая книга");

        savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setIsbn("9785043333333");
        savedBook.setTitle("Первая книга");

        bookResponseDto = new BookResponseDto();
        bookResponseDto.setId(1L);
        bookResponseDto.setIsbn("9785043333333");
        bookResponseDto.setTitle("Первая книга");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void createBook_Success() {
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.createBook(validBookDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void createBook_IsbnExists_ThrowsConflict() {
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.createBook(validBookDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void createBook_PublisherNotFound_ThrowsEntityNotFound() {
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(publisherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.createBook(validBookDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Publisher not found");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getAllBooks_Success() {
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);
        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(any(Book.class))).thenReturn(bookResponseDto);

        Page<BookResponseDto> result = bookService.getAllBooks(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void getAllBooks_EmptyPage_ReturnsEmptyPage() {
        Page<Book> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(bookRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<BookResponseDto> result = bookService.getAllBooks(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void getBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookById_NotFound_ThrowsEntityNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book not found with id: 999");
    }

    @Test
    void updateBook_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.updateBook(1L, validBookDto);

        assertThat(result).isNotNull();
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_NotFound_ThrowsEntityNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(999L, validBookDto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateBook_IsbnExists_ThrowsConflict() {
        BookDto updatedBookDto = new BookDto();
        updatedBookDto.setIsbn("9785043333999");
        updatedBookDto.setTitle("Updated Title");
        updatedBookDto.setDescription("Updated Description");
        updatedBookDto.setPublicationYear(2025);
        updatedBookDto.setPrice(new BigDecimal("600"));
        updatedBookDto.setPublisherId(1L);
        updatedBookDto.setAuthorIds(Set.of(1L));

        Book existingBookWithSameIsbn = new Book();
        existingBookWithSameIsbn.setId(2L);
        existingBookWithSameIsbn.setIsbn("9785043333999");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.findByIsbn("9785043333999")).thenReturn(Optional.of(existingBookWithSameIsbn));

        assertThatThrownBy(() -> bookService.updateBook(1L, updatedBookDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Book with ISBN 9785043333999 already exists");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_WithSameIsbn_DoesNotCheckConflict() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.updateBook(1L, validBookDto);

        assertThat(result).isNotNull();
        verify(bookRepository, never()).findByIsbn(validBookDto.getIsbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void deleteBook_Success() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        bookService.deleteBook(1L);

        verify(bookRepository).deleteById(1L);
    }

    @Test
    void deleteBook_NotFound_ThrowsEntityNotFound() {
        when(bookRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findBooksByAuthor_Success() {
        when(bookRepository.findBooksByComplexCriteria(("Test Author"), (null), (null), (null), (null), (null)))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.findBooksByAuthor("Test Author");

        assertThat(result).hasSize(1);
    }

    @Test
    void findBooksByGenre_Success() {
        when(bookRepository.findBooksByComplexCriteria((null), ("Test Genre"), (null), (null), (null), (null)))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.findBooksByGenre("Test Genre");

        assertThat(result).hasSize(1);
    }

    @Test
    void findBooksByPriceRange_Success() {
        when(bookRepository.findBooksByComplexCriteria((null), (null), (null), (new BigDecimal("10")), (new BigDecimal("100")), (null)))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.findBooksByPriceRange(new BigDecimal("10"), new BigDecimal("100"));

        assertThat(result).hasSize(1);
    }
    @Test
    void searchBooks_Success() {
        BookSearchCriteria criteria = new BookSearchCriteria("Test", null, null, null, null, null);
        when(bookRepository.findBooksByComplexCriteria(("Test"), (null), (null), (null), (null), (null)))
                .thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        // Исправлено: убираем pageable
        List<BookResponseDto> result = bookService.searchBooks(criteria);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchBooksWithPagination_Success() {
        BookSearchCriteria criteria = new BookSearchCriteria("Test", null, null, null, null, null);
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.findBooksByComplexCriteriaWithPagination(
                any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(bookPage);

        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        Page<BookResponseDto> result = bookService.searchBooksWithPagination(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void searchBooksNative_Success() {
        BookSearchCriteria criteria = new BookSearchCriteria("Test", null, null, null, null, null);
        Object[] row = new Object[13];

        when(bookRepository.findBooksByComplexCriteriaNative(
                any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(row));

        when(bookMapper.mapToBook(any(Object[].class))).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.searchBooksNative(criteria);

        assertThat(result).hasSize(1);
    }

    @Test
    void searchBooksNativeWithPagination_Success() {
        BookSearchCriteria criteria = new BookSearchCriteria("Test", null, null, null, null, null);
        Object[] row = new Object[13];
        List<Object[]> rowList = Collections.singletonList(row);
        Page<Object[]> page = new PageImpl<>(rowList, pageable, 1);

        when(bookRepository.findBooksByComplexCriteriaNativeWithPagination(
                any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        when(bookMapper.mapToBook(any(Object[].class))).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        Page<BookResponseDto> result = bookService.searchBooksNativeWithPagination(criteria, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void bulkCreateBooks_Success() {
        List<BookDto> bookDtos = List.of(validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        BulkCreateResultDto result = bookService.bulkCreateBooks(bookDtos);

        assertThat(result.getTotalSuccess()).isEqualTo(1);
        assertThat(result.getTotalFailed()).isZero();
    }

    @Test
    void bulkCreateBooks_WhenDuplicateExists_ThrowsConflict() {
        List<BookDto> bookDtos = List.of(validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.bulkCreateBooks(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);
    }

    @Test
    void bulkCreateBooks_WhenPublisherNotFound_RollsBack() {
        List<BookDto> bookDtos = List.of(validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.bulkCreateBooks(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Transaction rolled back");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void bulkCreateBooks_WhenAuthorNotFound_RollsBack() {
        List<BookDto> bookDtos = List.of(validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of());
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);

        assertThatThrownBy(() -> bookService.bulkCreateBooks(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Transaction rolled back");

        verify(bookRepository, never()).save(any(Book.class));
        // Убираем проверку findByIsbn, так как он вызывается всегда
    }

    @Test
    void bulkCreateBooksWithoutTransaction_AllBooksValid_ReturnsSuccess() {
        List<BookDto> bookDtos = List.of(validBookDto, anotherValidBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn(anotherValidBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(bookMapper.toEntity(anotherValidBookDto)).thenReturn(anotherBook);
        when(bookRepository.save(any(Book.class))).thenReturn(book, anotherBook);

        BulkCreateResultDto result = bookService.bulkCreateBooksWithoutTransaction(bookDtos);

        assertThat(result.getTotalSuccess()).isEqualTo(2);
        assertThat(result.getTotalFailed()).isZero();
        verify(bookRepository, times(2)).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithoutTransaction_WhenDuplicateExists_ThrowsConflictButFirstSaved() {
        BookDto firstBookDto = new BookDto();
        firstBookDto.setIsbn("9785043333331");
        firstBookDto.setTitle("Первая книга");
        firstBookDto.setPrice(new BigDecimal("500"));
        firstBookDto.setPublisherId(1L);
        firstBookDto.setAuthorIds(Set.of(1L));

        BookDto duplicateBookDto = new BookDto();
        duplicateBookDto.setIsbn("9785043333331");
        duplicateBookDto.setTitle("Дубликат книги");
        duplicateBookDto.setPrice(new BigDecimal("700"));
        duplicateBookDto.setPublisherId(1L);
        duplicateBookDto.setAuthorIds(Set.of(1L));

        List<BookDto> bookDtos = List.of(firstBookDto, duplicateBookDto);

        Book firstBook = new Book();
        firstBook.setId(1L);
        firstBook.setIsbn("9785043333331");
        firstBook.setTitle("Первая книга");

        when(bookRepository.findByIsbn("9785043333331"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(firstBook));

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookMapper.toEntity(firstBookDto)).thenReturn(firstBook);
        when(bookRepository.save(any(Book.class))).thenReturn(firstBook);

        assertThatThrownBy(() -> bookService.bulkCreateBooksWithoutTransaction(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithoutTransaction_WhenDataIntegrityError_ThrowsConflict() {
        List<BookDto> bookDtos = List.of(validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(bookRepository.save(any(Book.class))).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThatThrownBy(() -> bookService.bulkCreateBooksWithoutTransaction(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithoutTransaction_WhenEntityNotFound_ThrowsConflict() {
        List<BookDto> bookDtos = List.of(validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of());
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);

        assertThatThrownBy(() -> bookService.bulkCreateBooksWithoutTransaction(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithoutTransaction_WhenUnexpectedError_ThrowsConflict() {
        List<BookDto> bookDtos = List.of(validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(validBookDto)).thenThrow(new RuntimeException("Unexpected mapping error"));

        assertThatThrownBy(() -> bookService.bulkCreateBooksWithoutTransaction(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithTransaction_AllBooksValid_ReturnsSuccess() {
        List<BookDto> bookDtos = List.of(validBookDto, anotherValidBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn(anotherValidBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(bookMapper.toEntity(anotherValidBookDto)).thenReturn(anotherBook);
        when(bookRepository.save(any(Book.class))).thenReturn(book, anotherBook);

        BulkCreateResultDto result = bookService.bulkCreateBooksWithTransaction(bookDtos);

        assertThat(result.getTotalSuccess()).isEqualTo(2);
        assertThat(result.getTotalFailed()).isZero();
        verify(bookRepository, times(2)).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithTransaction_WhenDuplicateExists_ThrowsConflictAndNothingSaved() {
        List<BookDto> bookDtos = List.of(validBookDto, validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.bulkCreateBooksWithTransaction(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Transaction rolled back");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithTransaction_WhenPublisherNotFound_ThrowsException() {
        List<BookDto> bookDtos = List.of(validBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.bulkCreateBooksWithTransaction(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Transaction rolled back");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithTransaction_WhenSaveFails_RollsBack() {
        List<BookDto> bookDtos = List.of(validBookDto, anotherValidBookDto);

        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.findByIsbn(anotherValidBookDto.getIsbn())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(bookMapper.toEntity(anotherValidBookDto)).thenReturn(anotherBook);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookRepository.save(anotherBook)).thenThrow(new RuntimeException("Database error"));

        assertThatThrownBy(() -> bookService.bulkCreateBooksWithTransaction(bookDtos))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Transaction rolled back");

        verify(bookRepository, times(1)).save(book);
        verify(bookRepository, times(1)).save(anotherBook);
    }

    @Test
    void createBookWithoutTransaction_Success() {
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher);
        when(authorRepository.save(any(Author.class))).thenReturn(author);
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.createBookWithoutTransaction(validBookDto);

        assertThat(result).isNotNull();
        verify(publisherRepository, times(1)).save(any(Publisher.class));
        verify(authorRepository, times(1)).save(any(Author.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createBookWithoutTransaction_WhenDuplicateExists_ThrowsConflict() {
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher);
        when(authorRepository.save(any(Author.class))).thenReturn(author);
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.createBookWithoutTransaction(validBookDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void createBookWithTransaction_Success() {
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher);
        when(authorRepository.save(any(Author.class))).thenReturn(author);
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.createBookWithTransaction(validBookDto);

        assertThat(result).isNotNull();
        verify(publisherRepository, times(1)).save(any(Publisher.class));
        verify(authorRepository, times(1)).save(any(Author.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createBookWithTransaction_WhenDuplicateExists_ThrowsConflict() {
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher);
        when(authorRepository.save(any(Author.class))).thenReturn(author);
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.createBookWithTransaction(validBookDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getAllBooksWithDetails_Success() {
        when(bookRepository.findAllWithDetails()).thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.getAllBooksWithDetails();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllBooksWithDetails_EmptyResult() {
        when(bookRepository.findAllWithDetails()).thenReturn(List.of());

        List<BookResponseDto> result = bookService.getAllBooksWithDetails();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllBooksWithNPlus1Problem_Success() {
        when(bookRepository.findAll()).thenReturn(List.of(book));
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        List<BookResponseDto> result = bookService.getAllBooksWithNPlus1Problem();

        assertThat(result).hasSize(1);
    }

    @Test
    void updateBook_WithNullPublisherId_Success() {
        BookDto updateDto = new BookDto();
        updateDto.setIsbn(book.getIsbn());
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setPublicationYear(2025);
        updateDto.setPrice(new BigDecimal("600"));
        updateDto.setPublisherId(null);
        updateDto.setAuthorIds(Set.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.updateBook(1L, updateDto);

        assertThat(result).isNotNull();
        verify(publisherRepository, never()).findById(any());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_WithNullAuthorIds_Success() {
        BookDto updateDto = new BookDto();
        updateDto.setIsbn(book.getIsbn());
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setPublicationYear(2025);
        updateDto.setPrice(new BigDecimal("600"));
        updateDto.setPublisherId(1L);
        updateDto.setAuthorIds(null);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.updateBook(1L, updateDto);

        assertThat(result).isNotNull();
        verify(authorRepository, never()).findAllById(any());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void getAllBooksWithDetails_EmptyResult_ShouldReturnEmptyList() {
        when(bookRepository.findAllWithDetails()).thenReturn(List.of());

        List<BookResponseDto> result = bookService.getAllBooksWithDetails();

        assertThat(result).isEmpty();
        verify(bookRepository).findAllWithDetails();
    }

    @Test
    void createBook_WithNullAuthorIds_ShouldSkipAuthorProcessing() {
        BookDto bookDtoWithoutAuthors = new BookDto();
        bookDtoWithoutAuthors.setIsbn("9785043333999");
        bookDtoWithoutAuthors.setTitle("Book Without Authors");
        bookDtoWithoutAuthors.setDescription("Description");
        bookDtoWithoutAuthors.setPublicationYear(2024);
        bookDtoWithoutAuthors.setPrice(new BigDecimal("500"));
        bookDtoWithoutAuthors.setPublisherId(1L);
        bookDtoWithoutAuthors.setAuthorIds(null); // null authorIds

        Book newBook = new Book();
        newBook.setIsbn("9785043333999");
        newBook.setTitle("Book Without Authors");

        when(bookRepository.findByIsbn(bookDtoWithoutAuthors.getIsbn())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(bookDtoWithoutAuthors)).thenReturn(newBook);
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(bookRepository.save(any(Book.class))).thenReturn(newBook);
        when(bookMapper.toDto(any(Book.class))).thenReturn(new BookResponseDto());

        BookResponseDto result = bookService.createBook(bookDtoWithoutAuthors);

        assertThat(result).isNotNull();
        // Проверяем, что authorRepository не вызывался
        verify(authorRepository, never()).findAllById(any());
    }

    @Test
    void createBook_WithEmptyAuthorIds_ShouldSkipAuthorProcessing() {
        BookDto bookDtoWithEmptyAuthors = new BookDto();
        bookDtoWithEmptyAuthors.setIsbn("9785043333998");
        bookDtoWithEmptyAuthors.setTitle("Book With Empty Authors");
        bookDtoWithEmptyAuthors.setDescription("Description");
        bookDtoWithEmptyAuthors.setPublicationYear(2024);
        bookDtoWithEmptyAuthors.setPrice(new BigDecimal("500"));
        bookDtoWithEmptyAuthors.setPublisherId(1L);
        bookDtoWithEmptyAuthors.setAuthorIds(Set.of()); // пустой Set authorIds

        Book newBook = new Book();
        newBook.setIsbn("9785043333998");
        newBook.setTitle("Book With Empty Authors");

        when(bookRepository.findByIsbn(bookDtoWithEmptyAuthors.getIsbn())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(bookDtoWithEmptyAuthors)).thenReturn(newBook);
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(bookRepository.save(any(Book.class))).thenReturn(newBook);
        when(bookMapper.toDto(any(Book.class))).thenReturn(new BookResponseDto());

        BookResponseDto result = bookService.createBook(bookDtoWithEmptyAuthors);

        assertThat(result).isNotNull();
        // Проверяем, что authorRepository не вызывался
        verify(authorRepository, never()).findAllById(any());
    }

    @Test
    void setBookRelations_WithPartialAuthors_ThrowsEntityNotFound() {
        BookDto invalidBookDto = new BookDto();
        invalidBookDto.setIsbn("9785043333999");
        invalidBookDto.setTitle("Test Book");
        invalidBookDto.setPublisherId(1L);
        invalidBookDto.setAuthorIds(Set.of(1L, 2L));

        when(bookRepository.findByIsbn(any())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(invalidBookDto)).thenReturn(new Book());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(author));

        assertThatThrownBy(() -> bookService.createBook(invalidBookDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Some authors not found");
    }

    @Test
    void createBook_WithGenreIds_Success() {
        validBookDto.setGenreIds(Set.of(1L));
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookMapper.toDto(savedBook)).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.createBook(validBookDto);

        assertThat(result).isNotNull();
        verify(genreRepository).findAllById(Set.of(1L));
    }

    @Test
    void createBook_WithPartialGenreIds_ThrowsEntityNotFound() {
        validBookDto.setGenreIds(Set.of(1L, 2L));
        when(bookRepository.findByIsbn(validBookDto.getIsbn())).thenReturn(Optional.empty());
        when(bookMapper.toEntity(validBookDto)).thenReturn(book);
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(genreRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(genre));

        assertThatThrownBy(() -> bookService.createBook(validBookDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Some genres not found");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_WithEmptyGenreIds_ClearsGenres() {
        BookDto updateDto = new BookDto();
        updateDto.setIsbn(book.getIsbn());
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setPublicationYear(2025);
        updateDto.setPrice(new BigDecimal("600"));
        updateDto.setPublisherId(1L);
        updateDto.setAuthorIds(Set.of(1L));
        updateDto.setGenreIds(Set.of());

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookResponseDto);

        bookService.updateBook(1L, updateDto);

        verify(genreRepository, never()).findAllById(any());
        verify(bookRepository).save(argThat(b -> b.getGenres() != null && b.getGenres().isEmpty()));
    }

    // ============ ТЕСТЫ ДЛЯ ПОЛНОГО ПОКРЫТИЯ СТРОКИ 83 (updateBook проверка ISBN) ============

    @Test
    void updateBook_WhenIsbnChangedAndDoesNotExist_ShouldUpdateSuccessfully() {
        // Книга в БД с ISBN "9785043333333"
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setIsbn("9785043333333");
        existingBook.setTitle("Old Title");
        existingBook.setPublisher(publisher);
        existingBook.setAuthors(Set.of(author));

        // DTO с новым ISBN, которого нет в БД
        BookDto updateDto = new BookDto();
        updateDto.setIsbn("9785043333999"); // Новый ISBN
        updateDto.setTitle("New Title");
        updateDto.setDescription("New Description");
        updateDto.setPublicationYear(2025);
        updateDto.setPrice(new BigDecimal("600"));
        updateDto.setPublisherId(1L);
        updateDto.setAuthorIds(Set.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn("9785043333999")).thenReturn(Optional.empty()); // Новый ISBN не существует
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);
        when(bookMapper.toDto(any(Book.class))).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.updateBook(1L, updateDto);

        assertThat(result).isNotNull();
        // Проверяем, что проверка ISBN выполнялась
        verify(bookRepository).findByIsbn("9785043333999");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_WhenIsbnNotChanged_ShouldSkipIsbnCheck() {
        // Книга в БД с ISBN "9785043333333"
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setIsbn("9785043333333");
        existingBook.setTitle("Old Title");
        existingBook.setPublisher(publisher);
        existingBook.setAuthors(Set.of(author));

        // DTO с тем же ISBN
        BookDto updateDto = new BookDto();
        updateDto.setIsbn("9785043333333"); // Тот же ISBN
        updateDto.setTitle("New Title");
        updateDto.setDescription("New Description");
        updateDto.setPublicationYear(2025);
        updateDto.setPrice(new BigDecimal("600"));
        updateDto.setPublisherId(1L);
        updateDto.setAuthorIds(Set.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        // НЕ мокаем findByIsbn, так как он не должен вызываться
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(Set.of(1L))).thenReturn(List.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);
        when(bookMapper.toDto(any(Book.class))).thenReturn(bookResponseDto);

        BookResponseDto result = bookService.updateBook(1L, updateDto);

        assertThat(result).isNotNull();
        // Проверяем, что findByIsbn НЕ вызывался, так как ISBN не изменился
        verify(bookRepository, never()).findByIsbn(any());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_WhenIsbnChangedAndExists_ThrowsConflict() {
        // Книга в БД с ISBN "9785043333333"
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setIsbn("9785043333333");
        existingBook.setTitle("Old Title");

        // Другая книга с новым ISBN уже существует
        Book anotherBookWithNewIsbn = new Book();
        anotherBookWithNewIsbn.setId(2L);
        anotherBookWithNewIsbn.setIsbn("9785043333999");

        // DTO с новым ISBN, который уже существует у другой книги
        BookDto updateDto = new BookDto();
        updateDto.setIsbn("9785043333999");
        updateDto.setTitle("New Title");
        updateDto.setDescription("New Description");
        updateDto.setPublicationYear(2025);
        updateDto.setPrice(new BigDecimal("600"));
        updateDto.setPublisherId(1L);
        updateDto.setAuthorIds(Set.of(1L));

        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbn("9785043333999")).thenReturn(Optional.of(anotherBookWithNewIsbn));

        assertThatThrownBy(() -> bookService.updateBook(1L, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Book with ISBN 9785043333999 already exists");

        verify(bookRepository, never()).save(any(Book.class));
    }

}