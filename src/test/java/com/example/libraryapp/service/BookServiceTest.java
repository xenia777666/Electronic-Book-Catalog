package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookDto;
import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.BulkCreateResultDto;
import com.example.libraryapp.api.mapper.BookMapper;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.domain.Publisher;
import com.example.libraryapp.repository.AuthorRepository;
import com.example.libraryapp.repository.BookRepository;
import com.example.libraryapp.repository.GenreRepository;
import com.example.libraryapp.repository.PublisherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @InjectMocks
    private BookService bookService;

    private BookDto validBookDto;
    private Book validBook;
    private BookResponseDto validBookResponseDto;
    private Publisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Эксмо");

        validBookDto = new BookDto();
        validBookDto.setIsbn("978-5-699-18031-2");
        validBookDto.setTitle("Мастер и Маргарита");
        validBookDto.setPrice(new BigDecimal("450.00"));
        validBookDto.setPublisherId(1L);
        validBookDto.setAuthorIds(Set.of(1L));

        validBook = new Book();
        validBook.setId(1L);
        validBook.setIsbn(validBookDto.getIsbn());
        validBook.setTitle(validBookDto.getTitle());

        validBookResponseDto = new BookResponseDto();
        validBookResponseDto.setId(1L);
        validBookResponseDto.setIsbn(validBookDto.getIsbn());
        validBookResponseDto.setTitle(validBookDto.getTitle());
    }

    // ============= ТЕСТЫ ДЛЯ BULK-ОПЕРАЦИИ =============

    @Test
    void bulkCreateBooks_ShouldReturnSuccess_WhenAllBooksValid() {
        // given
        List<BookDto> booksDto = List.of(validBookDto);
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));

        // ✅ Исправлено: doReturn для коллекций
        doReturn(Collections.emptySet()).when(authorRepository).findAllById(anySet());
        doReturn(Collections.emptySet()).when(genreRepository).findAllById(anySet());

        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(validBook);
        when(bookRepository.saveAll(anyList())).thenReturn(List.of(validBook));
        when(bookMapper.toDto(any(Book.class))).thenReturn(validBookResponseDto);

        // when
        BulkCreateResultDto result = bookService.bulkCreateBooks(booksDto);

        // then
        assertThat(result.getTotalRequested()).isEqualTo(1);
        assertThat(result.getSuccessful()).isEqualTo(1);
        assertThat(result.getFailed()).isZero();
        assertThat(result.getCreatedBooks()).hasSize(1);
    }

    @Test
    void bulkCreateBooks_ShouldSkipDuplicate_WhenIsbnExists() {
        // given
        List<BookDto> booksDto = List.of(validBookDto);
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(validBook));

        // when
        BulkCreateResultDto result = bookService.bulkCreateBooks(booksDto);

        // then
        assertThat(result.getTotalRequested()).isEqualTo(1);
        assertThat(result.getSuccessful()).isZero();
        assertThat(result.getFailed()).isEqualTo(1);
        verify(bookRepository, never()).saveAll(anyList());
    }

    @Test
    void bulkCreateBooks_ShouldReturnPartialSuccess_WhenSomeInvalid() {
        // given
        List<BookDto> booksDto = List.of(validBookDto, validBookDto);
        when(bookRepository.findByIsbn(anyString()))
                .thenReturn(Optional.empty())   // первая книга — ок
                .thenReturn(Optional.of(validBook)); // вторая — дубликат
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));

        // ✅ Исправлено: doReturn для коллекций
        doReturn(Collections.emptySet()).when(authorRepository).findAllById(anySet());
        doReturn(Collections.emptySet()).when(genreRepository).findAllById(anySet());

        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(validBook);
        when(bookRepository.saveAll(anyList())).thenReturn(List.of(validBook));
        when(bookMapper.toDto(any(Book.class))).thenReturn(validBookResponseDto);

        // when
        BulkCreateResultDto result = bookService.bulkCreateBooks(booksDto);

        // then
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getSuccessful()).isEqualTo(1);
        assertThat(result.getFailed()).isEqualTo(1);
    }

    // ============= ТЕСТЫ ДЛЯ ДЕМОНСТРАЦИИ ТРАНЗАКЦИЙ =============

    @Test
    void bulkCreateBooksWithoutTransaction_ShouldReturnPartialSuccess_WhenSomeFail() {
        // given
        List<BookDto> booksDto = List.of(validBookDto, validBookDto);
        when(bookRepository.findByIsbn(anyString()))
                .thenReturn(Optional.empty())   // первая книга — ок
                .thenReturn(Optional.of(validBook)); // вторая — дубликат
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));

        // ✅ Исправлено: doReturn для коллекций
        doReturn(Collections.emptySet()).when(authorRepository).findAllById(anySet());
        doReturn(Collections.emptySet()).when(genreRepository).findAllById(anySet());

        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(validBook);
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);
        when(bookMapper.toDto(any(Book.class))).thenReturn(validBookResponseDto);

        // when
        BulkCreateResultDto result = bookService.bulkCreateBooksWithoutTransaction(booksDto);

        // then
        assertThat(result.getTotalRequested()).isEqualTo(2);
        assertThat(result.getSuccessful()).isEqualTo(1);
        assertThat(result.getFailed()).isEqualTo(1);
        assertThat(result.getErrors()).hasSize(1);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void bulkCreateBooksWithTransaction_ShouldThrowException_WhenAnyDuplicate() {
        // given
        List<BookDto> booksDto = List.of(validBookDto, validBookDto);
        when(bookRepository.findByIsbn(anyString()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(validBook));

        // when & then
        assertThatThrownBy(() -> bookService.bulkCreateBooksWithTransaction(booksDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Обнаружены дубликаты ISBN");

        verify(bookRepository, never()).saveAll(anyList());
    }

    @Test
    void bulkCreateBooksWithTransaction_ShouldSaveAll_WhenAllValid() {
        // given
        List<BookDto> booksDto = List.of(validBookDto);
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));

        // ✅ Исправлено: doReturn для коллекций
        doReturn(Collections.emptySet()).when(authorRepository).findAllById(anySet());
        doReturn(Collections.emptySet()).when(genreRepository).findAllById(anySet());

        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(validBook);
        when(bookRepository.saveAll(anyList())).thenReturn(List.of(validBook));
        when(bookMapper.toDto(any(Book.class))).thenReturn(validBookResponseDto);

        // when
        BulkCreateResultDto result = bookService.bulkCreateBooksWithTransaction(booksDto);

        // then
        assertThat(result.getSuccessful()).isEqualTo(1);
        assertThat(result.getFailed()).isZero();
        verify(bookRepository, times(1)).saveAll(anyList());
    }

    // ============= ТЕСТЫ ДЛЯ CRUD =============

    @Test
    void createBook_ShouldReturnCreatedBook_WhenValid() {
        // given
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));

        // ✅ Исправлено: doReturn для коллекций
        doReturn(Collections.emptySet()).when(authorRepository).findAllById(anySet());
        doReturn(Collections.emptySet()).when(genreRepository).findAllById(anySet());

        when(bookMapper.toEntity(any(BookDto.class))).thenReturn(validBook);
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);
        when(bookMapper.toDto(any(Book.class))).thenReturn(validBookResponseDto);

        // when
        BookResponseDto result = bookService.createBook(validBookDto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Мастер и Маргарита");
        verify(indexService).invalidateCache();
    }

    @Test
    void createBook_ShouldThrowConflict_WhenIsbnExists() {
        // given
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.of(validBook));

        // when & then
        assertThatThrownBy(() -> bookService.createBook(validBookDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    void getBookById_ShouldReturnBook_WhenExists() {
        // given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(validBook));
        when(bookMapper.toDto(any(Book.class))).thenReturn(validBookResponseDto);

        // when
        BookResponseDto result = bookService.getBookById(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookById_ShouldThrowNotFound_WhenNotExists() {
        // given
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> bookService.getBookById(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteBook_ShouldDelete_WhenExists() {
        // given
        when(bookRepository.existsById(1L)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1L);

        // when
        bookService.deleteBook(1L);

        // then
        verify(bookRepository).deleteById(1L);
        verify(indexService).invalidateCache();
    }

    @Test
    void deleteBook_ShouldThrowNotFound_WhenNotExists() {
        // given
        when(bookRepository.existsById(999L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }
}