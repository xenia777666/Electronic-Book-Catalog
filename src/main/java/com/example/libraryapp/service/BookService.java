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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    // ============= ЛАБА 3: Сложный поиск с фильтрацией и кэшированием =============
    public List<BookResponseDto> searchBooks(BookSearchCriteria criteria, Pageable pageable) {
        log.info("Searching books with criteria: {}", criteria);

        // Пытаемся получить из кэша
        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        if (cached != null) {
            return cached;
        }

        // Если в кэше нет — выполняем запрос
        List<Book> books = bookRepository.findBooksByComplexCriteria(
                criteria.getAuthorName(),
                criteria.getGenreName(),
                criteria.getPublisherName(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinRating()
        );

        List<BookResponseDto> results = books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());

        // Сохраняем в кэш
        indexService.putInCache(criteria, pageable, results);

        return results;
    }

    // ============= ЛАБА 3: То же с пагинацией =============
    public Page<BookResponseDto> searchBooksWithPagination(
            BookSearchCriteria criteria, Pageable pageable) {
        log.info("Searching books with criteria and pagination: {}, page: {}, size: {}",
                criteria, pageable.getPageNumber(), pageable.getPageSize());

        return bookRepository.findBooksByComplexCriteriaWithPagination(
                criteria.getAuthorName(),
                criteria.getGenreName(),
                criteria.getPublisherName(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinRating(),
                pageable
        ).map(bookMapper::toDto);
    }

    // ============= ЛАБА 3: Native query =============
    public List<BookResponseDto> searchBooksNative(BookSearchCriteria criteria) {
        log.info("Searching books with native query, criteria: {}", criteria);

        return bookRepository.findBooksByComplexCriteriaNative(
                        criteria.getAuthorName(),
                        criteria.getGenreName(),
                        criteria.getPublisherName(),
                        criteria.getMinPrice(),
                        criteria.getMaxPrice(),
                        criteria.getMinRating()
                ).stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookResponseDto createBook(BookDto bookDto) {
        log.info("Creating new book: {}", bookDto.getTitle());
        Book book = bookMapper.toEntity(bookDto);

        Publisher publisher = publisherRepository.findById(bookDto.getPublisherId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Publisher not found with id: " + bookDto.getPublisherId()
                ));
        book.setPublisher(publisher);

        Set<Author> authors = new HashSet<>(authorRepository.findAllById(bookDto.getAuthorIds()));
        if (authors.size() != bookDto.getAuthorIds().size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Some authors not found for ids: " + bookDto.getAuthorIds()
            );
        }
        book.setAuthors(authors);

        if (bookDto.getGenreIds() != null && !bookDto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(bookDto.getGenreIds()));
            if (genres.size() != bookDto.getGenreIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Some genres not found for ids: " + bookDto.getGenreIds()
                );
            }
            book.setGenres(genres);
        }

        // Инвалидируем кэш при создании
        indexService.invalidateCache();

        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with id: {}", savedBook.getId());
        return bookMapper.toDto(savedBook);
    }

    public Page<BookResponseDto> getAllBooks(Pageable pageable) {
        log.debug("Getting all books with pagination: page {}, size {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(pageable)
                .map(bookMapper::toDto);
    }

    public BookResponseDto getBookById(Long id) {
        log.debug("Getting book by id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found with id: " + id
                ));
        return bookMapper.toDto(book);
    }

    @Transactional
    public BookResponseDto updateBook(Long id, BookDto bookDto) {
        log.info("Updating book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Book not found with id: " + id
                ));

        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setDescription(bookDto.getDescription());
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setPrice(bookDto.getPrice());

        Publisher publisher = publisherRepository.findById(bookDto.getPublisherId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Publisher not found with id: " + bookDto.getPublisherId()
                ));
        book.setPublisher(publisher);

        Set<Author> authors = new HashSet<>(authorRepository.findAllById(bookDto.getAuthorIds()));
        if (authors.size() != bookDto.getAuthorIds().size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Some authors not found for ids: " + bookDto.getAuthorIds()
            );
        }
        book.setAuthors(authors);

        if (bookDto.getGenreIds() != null && !bookDto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(bookDto.getGenreIds()));
            if (genres.size() != bookDto.getGenreIds().size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Some genres not found for ids: " + bookDto.getGenreIds()
                );
            }
            book.setGenres(genres);
        }

        // Инвалидируем кэш при обновлении
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
                    "Book not found with id: " + id
            );
        }
        bookRepository.deleteById(id);

        // Инвалидируем кэш при удалении
        indexService.invalidateCache();

        log.info("Book deleted successfully");
    }

    public List<BookResponseDto> findBooksByAuthor(String authorName) {
        log.debug("Searching books by author: {}", authorName);
        return bookRepository.findByAuthorName(authorName)
                .stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> findBooksByGenre(String genreName) {
        log.debug("Searching books by genre: {}", genreName);
        return bookRepository.findByGenreName(genreName)
                .stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> findBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Searching books by price range: {} - {}", minPrice, maxPrice);
        return bookRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> getAllBooksWithDetails() {
        log.info("Getting all books with details (authors, genres, publisher)");
        List<Book> books = bookRepository.findAllWithDetails();
        log.debug("Loaded {} books with details", books.size());
        return books.stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<BookResponseDto> getAllBooksWithNPlus1Problem() {
        log.warn("DEMONSTRATING N+1 PROBLEM");
        List<Book> books = bookRepository.findAll();

        for (Book book : books) {
            log.debug("Loading authors for book {}: will cause additional query", book.getId());
            book.getAuthors().size();
            log.debug("Loading genres for book {}: will cause additional query", book.getId());
            book.getGenres().size();
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
        log.info("Temporary publisher saved with id: {}", tempPublisher.getId());

        Author tempAuthor = new Author();
        tempAuthor.setName("TEMP_AUTHOR_" + System.currentTimeMillis());
        tempAuthor = authorRepository.save(tempAuthor);
        log.info("Temporary author saved with id: {}", tempAuthor.getId());

        Book book = bookMapper.toEntity(bookDto);
        book.setPublisher(tempPublisher);
        book.getAuthors().add(tempAuthor);

        if (bookDto.getTitle() != null && bookDto.getTitle().contains("error")) {
            log.error("Simulating error during save!");
            throw new IllegalStateException("Simulated error during book creation");
        }

        Book savedBook = bookRepository.save(book);
        log.info("Book saved successfully");
        return savedBook;
    }

    @Transactional
    public Book createBookWithTransaction(BookDto bookDto) {
        log.info("DEMONSTRATING ATOMIC SAVE WITH @Transactional");

        Publisher tempPublisher = new Publisher();
        tempPublisher.setName("TEMP_" + System.currentTimeMillis());
        tempPublisher = publisherRepository.save(tempPublisher);
        log.info("Temporary publisher saved with id: {}", tempPublisher.getId());

        Author tempAuthor = new Author();
        tempAuthor.setName("TEMP_AUTHOR_" + System.currentTimeMillis());
        tempAuthor = authorRepository.save(tempAuthor);
        log.info("Temporary author saved with id: {}", tempAuthor.getId());

        Book book = bookMapper.toEntity(bookDto);
        book.setPublisher(tempPublisher);
        book.getAuthors().add(tempAuthor);

        if (bookDto.getTitle() != null && bookDto.getTitle().contains("error")) {
            log.error("Simulating error during save - transaction will rollback!");
            throw new IllegalStateException("Simulated error during book creation");
        }

        Book savedBook = bookRepository.save(book);
        log.info("Book saved successfully");
        return savedBook;
    }
}