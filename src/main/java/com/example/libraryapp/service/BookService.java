package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookDto;
import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.BookSearchCriteria;
import com.example.libraryapp.api.mapper.BookMapper;
import com.example.libraryapp.domain.Author;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.domain.Genre;
import com.example.libraryapp.domain.Publisher;
import com.example.libraryapp.repository.AuthorRepository;
import com.example.libraryapp.repository.BookRepository;
import com.example.libraryapp.repository.GenreRepository;
import com.example.libraryapp.repository.PublisherRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;
    private final IndexService indexService;

    // ============= УНИВЕРСАЛЬНЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ ДАННЫХ =============
    private List<BookResponseDto> executeAndCache(
            BookSearchCriteria criteria,
            Pageable pageable,
            Function<BookSearchCriteria, List<Book>> queryExecutor) {

        // 1. Пытаемся получить из кэша
        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        if (cached != null) {
            return cached;
        }

        // 2. Если в кэше нет — выполняем запрос
        List<Book> books = queryExecutor.apply(criteria);
        List<BookResponseDto> results = books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());

        // 3. Сохраняем в кэш
        indexService.putInCache(criteria, pageable, results);

        return results;
    }

    // ============= УНИВЕРСАЛЬНЫЙ МЕТОД ДЛЯ ПАГИНАЦИИ =============
    private Page<BookResponseDto> paginateResults(
            BookSearchCriteria criteria,
            Pageable pageable,
            List<BookResponseDto> allResults) {

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allResults.size());

        if (start >= allResults.size()) {
            return new PageImpl<>(List.of(), pageable, allResults.size());
        }

        return new PageImpl<>(allResults.subList(start, end), pageable, allResults.size());
    }

    // ============= JPQL ЗАПРОСЫ =============
    public List<BookResponseDto> searchBooks(BookSearchCriteria criteria, Pageable pageable) {
        log.info("JPQL search: {}", criteria);
        return executeAndCache(criteria, pageable,
                c -> bookRepository.findBooksByComplexCriteria(
                        c.getAuthorName(),
                        c.getGenreName(),
                        c.getPublisherName(),
                        c.getMinPrice(),
                        c.getMaxPrice(),
                        c.getMinRating()));
    }

    public Page<BookResponseDto> searchBooksWithPagination(BookSearchCriteria criteria, Pageable pageable) {
        log.info("JPQL search with pagination: {}, page: {}, size: {}",
                criteria, pageable.getPageNumber(), pageable.getPageSize());
        List<BookResponseDto> allResults = searchBooks(criteria, pageable);
        return paginateResults(criteria, pageable, allResults);
    }

    // ============= NATIVE ЗАПРОСЫ =============
    public List<BookResponseDto> searchBooksNative(BookSearchCriteria criteria) {
        log.info("Native search: {}", criteria);
        return executeAndCache(criteria, Pageable.unpaged(),
                c -> bookRepository.findBooksByComplexCriteriaNative(
                        c.getAuthorName(),
                        c.getGenreName(),
                        c.getPublisherName(),
                        c.getMinPrice(),
                        c.getMaxPrice(),
                        c.getMinRating()));
    }

    public Page<BookResponseDto> searchBooksNativeWithPagination(BookSearchCriteria criteria, Pageable pageable) {
        log.info("Native search with pagination: {}, page: {}, size: {}",
                criteria, pageable.getPageNumber(), pageable.getPageSize());
        List<BookResponseDto> allResults = searchBooksNative(criteria);
        return paginateResults(criteria, pageable, allResults);
    }

    // ============= УПРОЩЕННЫЕ ПОИСКИ (используют JPQL) =============
    public List<BookResponseDto> findBooksByAuthor(String authorName) {
        log.debug("Searching books by author: {}", authorName);
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setAuthorName(authorName);
        return searchBooks(criteria, Pageable.ofSize(20));
    }

    public List<BookResponseDto> findBooksByGenre(String genreName) {
        log.debug("Searching books by genre: {}", genreName);
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setGenreName(genreName);
        return searchBooks(criteria, Pageable.ofSize(20));
    }

    public List<BookResponseDto> findBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Searching books by price range: {} - {}", minPrice, maxPrice);
        BookSearchCriteria criteria = new BookSearchCriteria();
        criteria.setMinPrice(minPrice);
        criteria.setMaxPrice(maxPrice);
        return searchBooks(criteria, Pageable.ofSize(20));
    }

    // ============= CRUD МЕТОДЫ =============
    @Transactional
    public BookResponseDto createBook(BookDto bookDto) {
        log.info("Creating new book: {}", bookDto.getTitle());
        Book book = bookMapper.toEntity(bookDto);

        Publisher publisher = publisherRepository.findById(bookDto.getPublisherId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Publisher not found with id: " + bookDto.getPublisherId()));
        book.setPublisher(publisher);

        Set<Author> authors = new HashSet<>(authorRepository.findAllById(bookDto.getAuthorIds()));
        if (authors.size() != bookDto.getAuthorIds().size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Some authors not found for ids: " + bookDto.getAuthorIds());
        }
        book.setAuthors(authors);

        if (bookDto.getGenreIds() != null && !bookDto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(bookDto.getGenreIds()));
            if (genres.size() != bookDto.getGenreIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Some genres not found for ids: " + bookDto.getGenreIds());
            }
            book.setGenres(genres);
        }

        indexService.invalidateCache();
        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with id: {}", savedBook.getId());
        return bookMapper.toDto(savedBook);
    }

    public Page<BookResponseDto> getAllBooks(Pageable pageable) {
        log.debug("Getting all books: page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(pageable).map(bookMapper::toDto);
    }

    public BookResponseDto getBookById(Long id) {
        log.debug("Getting book by id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found with id: " + id));
        return bookMapper.toDto(book);
    }

    @Transactional
    public BookResponseDto updateBook(Long id, BookDto bookDto) {
        log.info("Updating book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found with id: " + id));

        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setDescription(bookDto.getDescription());
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setPrice(bookDto.getPrice());

        Publisher publisher = publisherRepository.findById(bookDto.getPublisherId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Publisher not found with id: " + bookDto.getPublisherId()));
        book.setPublisher(publisher);

        Set<Author> authors = new HashSet<>(authorRepository.findAllById(bookDto.getAuthorIds()));
        if (authors.size() != bookDto.getAuthorIds().size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Some authors not found for ids: " + bookDto.getAuthorIds());
        }
        book.setAuthors(authors);

        if (bookDto.getGenreIds() != null && !bookDto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(bookDto.getGenreIds()));
            if (genres.size() != bookDto.getGenreIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Some genres not found for ids: " + bookDto.getGenreIds());
            }
            book.setGenres(genres);
        }

        indexService.invalidateCache();
        Book updatedBook = bookRepository.save(book);
        log.info("Book updated successfully");
        return bookMapper.toDto(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        log.info("Deleting book with id: {}", id);
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        indexService.invalidateCache();
        log.info("Book deleted successfully");
    }

    public List<BookResponseDto> getAllBooksWithDetails() {
        log.info("Getting all books with details");
        return bookRepository.findAllWithDetails()
                .stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> getAllBooksWithNPlus1Problem() {
        log.warn("DEMONSTRATING N+1 PROBLEM");
        List<Book> books = bookRepository.findAll();

        for (Book book : books) {
            int authorsCount = book.getAuthors().size();
            int genresCount = book.getGenres().size();
            log.debug("Book {}: {} authors, {} genres", book.getId(), authorsCount, genresCount);
        }

        return books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    public Book createBookWithoutTransaction(BookDto bookDto) {
        log.warn("DEMONSTRATING PARTIAL SAVE WITHOUT @Transactional");

        Publisher tempPublisher = new Publisher();
        tempPublisher.setName("TEMP_" + System.currentTimeMillis());
        tempPublisher = publisherRepository.save(tempPublisher);
        log.info("Temporary publisher saved: {}", tempPublisher.getId());

        Author tempAuthor = new Author();
        tempAuthor.setName("TEMP_AUTHOR_" + System.currentTimeMillis());
        tempAuthor = authorRepository.save(tempAuthor);
        log.info("Temporary author saved: {}", tempAuthor.getId());

        Book book = bookMapper.toEntity(bookDto);
        book.setPublisher(tempPublisher);
        book.getAuthors().add(tempAuthor);

        if (bookDto.getTitle() != null && bookDto.getTitle().contains("error")) {
            log.error("Simulating error during save!");
            throw new IllegalStateException("Simulated error during book creation");
        }

        return bookRepository.save(book);
    }

    @Transactional
    public Book createBookWithTransaction(BookDto bookDto) {
        log.info("DEMONSTRATING ATOMIC SAVE WITH @Transactional");

        Publisher tempPublisher = new Publisher();
        tempPublisher.setName("TEMP_" + System.currentTimeMillis());
        tempPublisher = publisherRepository.save(tempPublisher);
        log.info("Temporary publisher saved: {}", tempPublisher.getId());

        Author tempAuthor = new Author();
        tempAuthor.setName("TEMP_AUTHOR_" + System.currentTimeMillis());
        tempAuthor = authorRepository.save(tempAuthor);
        log.info("Temporary author saved: {}", tempAuthor.getId());

        Book book = bookMapper.toEntity(bookDto);
        book.setPublisher(tempPublisher);
        book.getAuthors().add(tempAuthor);

        if (bookDto.getTitle() != null && bookDto.getTitle().contains("error")) {
            log.error("Simulating error during save - transaction will rollback!");
            throw new IllegalStateException("Simulated error during book creation");
        }

        return bookRepository.save(book);
    }
}