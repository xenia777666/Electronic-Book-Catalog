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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private static final String BOOK_CREATED_SUCCESSFULLY = "Book created successfully";

    private static String nullIfBlank(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;

    @Transactional
    public BookResponseDto createBook(BookDto bookDto) {
        log.info("Creating new book: {}", bookDto.getTitle());

        if (bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Book with ISBN " + bookDto.getIsbn() + " already exists"
            );
        }

        Book book = bookMapper.toEntity(bookDto);
        setBookRelations(book, bookDto);

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
        Book book = bookRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
        return bookMapper.toDto(book);
    }

    @Transactional
    public BookResponseDto updateBook(Long id, BookDto bookDto) {
        log.info("Updating book with id: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));

        if (!book.getIsbn().equals(bookDto.getIsbn())
                && bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Book with ISBN " + bookDto.getIsbn() + " already exists"
            );
        }

        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setDescription(bookDto.getDescription());
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setPrice(bookDto.getPrice());

        setBookRelations(book, bookDto);

        Book updatedBook = bookRepository.save(book);
        log.info("Book updated successfully");
        return bookMapper.toDto(updatedBook);
    }

    @Transactional
    public void deleteBook(Long id) {
        log.info("Deleting book with id: {}", id);

        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
        log.info("Book deleted successfully");
    }

    @Cacheable(value = "booksWithDetails", unless = "#result.isEmpty()")
    public List<BookResponseDto> getAllBooksWithDetails() {
        log.info("Getting all books with details (using JOIN FETCH)");
        return bookRepository.findAllWithDetails().stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public List<BookResponseDto> getAllBooksWithNPlus1Problem() {
        log.warn("DEMONSTRATING N+1 PROBLEM");
        return bookRepository.findAll().stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public List<BookResponseDto> findBooksByAuthor(String authorName) {
        log.debug("Searching books by author: {}", authorName);
        return bookRepository.findBooksByComplexCriteria(authorName, null, null, null, null, null).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public List<BookResponseDto> findBooksByGenre(String genreName) {
        log.debug("Searching books by genre: {}", genreName);
        return bookRepository.findBooksByComplexCriteria(null, genreName, null, null, null, null).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public List<BookResponseDto> findBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Searching books by price range: {} - {}", minPrice, maxPrice);
        return bookRepository.findBooksByComplexCriteria(null, null, null, minPrice, maxPrice, null).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public List<BookResponseDto> searchBooks(BookSearchCriteria criteria) {
        log.info("JPQL search: {}", criteria);
        return bookRepository.findBooksByComplexCriteria(
                        criteria.getAuthorName(),
                        criteria.getGenreName(),
                        nullIfBlank(criteria.getTitle()),
                        criteria.getMinPrice(),
                        criteria.getMaxPrice(),
                        criteria.getMinRating()).stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public Page<BookResponseDto> searchBooksWithPagination(BookSearchCriteria criteria, Pageable pageable) {
        log.info(
                "JPQL search with pagination: {}, page: {}, size: {}",
                criteria, pageable.getPageNumber(), pageable.getPageSize()
        );
        return bookRepository.findBooksByComplexCriteriaWithPagination(
                criteria.getAuthorName(),
                criteria.getGenreName(),
                nullIfBlank(criteria.getTitle()),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinRating(),
                pageable).map(bookMapper::toDto);
    }

    public List<BookResponseDto> searchBooksNative(BookSearchCriteria criteria) {
        log.info("Native search: {}", criteria);
        return bookRepository.findBooksByComplexCriteriaNative(
                        criteria.getAuthorName(),
                        criteria.getGenreName(),
                        nullIfBlank(criteria.getTitle()),
                        criteria.getMinPrice(),
                        criteria.getMaxPrice(),
                        criteria.getMinRating()).stream()
                .map(bookMapper::mapToBook)
                .map(bookMapper::toDto)
                .toList();
    }

    public Page<BookResponseDto> searchBooksNativeWithPagination(BookSearchCriteria criteria, Pageable pageable) {
        log.info(
                "Native search with pagination: {}, page: {}, size: {}",
                criteria, pageable.getPageNumber(), pageable.getPageSize()
        );
        return bookRepository.findBooksByComplexCriteriaNativeWithPagination(
                        criteria.getAuthorName(),
                        criteria.getGenreName(),
                        nullIfBlank(criteria.getTitle()),
                        criteria.getMinPrice(),
                        criteria.getMaxPrice(),
                        criteria.getMinRating(),
                        pageable)
                .map(bookMapper::mapToBook)
                .map(bookMapper::toDto);
    }

    public Book createBookWithoutTransaction(BookDto bookDto) {
        log.info("DEMONSTRATING SAVE WITHOUT @Transactional");
        return createBookWithTempPublisherAndAuthor(bookDto);
    }

    @Transactional
    public Book createBookWithTransaction(BookDto bookDto) {
        log.info("DEMONSTRATING ATOMIC SAVE WITH @Transactional");
        return createBookWithTempPublisherAndAuthor(bookDto);
    }

    @Transactional
    public BulkCreateResultDto bulkCreateBooks(List<BookDto> booksDto) {
        log.info("Bulk creating {} books WITH transaction", booksDto.size());
        return executeBulkCreate(booksDto, false);
    }

    public BulkCreateResultDto bulkCreateBooksWithoutTransaction(List<BookDto> booksDto) {
        log.info("Bulk creating {} books WITHOUT transaction", booksDto.size());
        return executeBulkCreate(booksDto, true);
    }

    @Transactional
    public BulkCreateResultDto bulkCreateBooksWithTransaction(List<BookDto> booksDto) {
        log.info("Bulk creating {} books WITH transaction (atomic)", booksDto.size());
        return executeBulkCreate(booksDto, false);
    }

    private Book createBookWithTempPublisherAndAuthor(BookDto bookDto) {
        Book book = bookMapper.toEntity(bookDto);

        Publisher tempPublisher = new Publisher();
        tempPublisher.setName("Temp Publisher " + System.currentTimeMillis());
        Publisher savedPublisher = publisherRepository.save(tempPublisher);
        log.info("Temporary publisher saved: {}", savedPublisher.getId());

        Author tempAuthor = new Author();
        tempAuthor.setName("Temp Author " + System.currentTimeMillis());
        Author savedAuthor = authorRepository.save(tempAuthor);
        log.info("Temporary author saved: {}", savedAuthor.getId());

        book.setPublisher(savedPublisher);
        book.setAuthors(Set.of(savedAuthor));

        if (bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Book with ISBN " + bookDto.getIsbn() + " already exists"
            );
        }

        return bookRepository.save(book);
    }

    private BulkCreateResultDto executeBulkCreate(List<BookDto> booksDto, boolean partialSuccessAllowed) {
        BulkCreateResultDto result = new BulkCreateResultDto();
        List<BulkCreateResultDto.BookResult> results = new ArrayList<>();

        if (partialSuccessAllowed) {
            return executeBulkCreateWithPartialSuccess(booksDto, results, result);
        } else {
            return executeBulkCreateAtomic(booksDto, results, result);
        }
    }

    private BulkCreateResultDto executeBulkCreateAtomic(List<BookDto> booksDto,
                                                        List<BulkCreateResultDto.BookResult> results,
                                                        BulkCreateResultDto result) {
        try {
            for (BookDto dto : booksDto) {
                if (bookRepository.findByIsbn(dto.getIsbn()).isPresent()) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Book with ISBN " + dto.getIsbn() + " already exists"
                    );
                }
                Book book = bookMapper.toEntity(dto);
                setBookRelations(book, dto);
                bookRepository.save(book);
                results.add(new BulkCreateResultDto.BookResult(
                        dto.getIsbn(), true, BOOK_CREATED_SUCCESSFULLY, null));
            }
            result.setResults(results);
            result.setTotalSuccess(results.size());
            result.setTotalFailed(0);
            result.setMessage(String.format("Successfully created %d books", results.size()));
            log.info("Bulk create WITH transaction: {} books saved atomically", results.size());
            return result;
        } catch (Exception e) {
            log.error("Error in transactional bulk create - rolling back everything: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Transaction rolled back: " + e.getMessage(), e);
        }
    }

    private BulkCreateResultDto executeBulkCreateWithPartialSuccess(List<BookDto> booksDto,
                                                                    List<BulkCreateResultDto.BookResult> results,
                                                                    BulkCreateResultDto result) {
        int successCount = 0;
        int failedCount = 0;
        List<String> errors = new ArrayList<>();

        for (BookDto dto : booksDto) {
            try {
                if (bookRepository.findByIsbn(dto.getIsbn()).isPresent()) {
                    String error = "Book with ISBN " + dto.getIsbn() + " already exists";
                    errors.add(error);
                    results.add(new BulkCreateResultDto.BookResult(
                            dto.getIsbn(), false, null, error));
                    failedCount++;
                    log.warn("Duplicate ISBN found: {}", dto.getIsbn());
                    continue;
                }

                Book book = bookMapper.toEntity(dto);
                setBookRelations(book, dto);
                bookRepository.save(book);
                results.add(new BulkCreateResultDto.BookResult(
                        dto.getIsbn(), true, BOOK_CREATED_SUCCESSFULLY, null));
                successCount++;
                log.info("Book saved: {}", book.getTitle());

            } catch (DataIntegrityViolationException e) {
                String error = "Data integrity violation for ISBN " + dto.getIsbn() + ": " + e.getMessage();
                errors.add(error);
                results.add(new BulkCreateResultDto.BookResult(
                        dto.getIsbn(), false, null, error));
                failedCount++;
                log.error("Data integrity error for ISBN {}: {}", dto.getIsbn(), e.getMessage());
            } catch (EntityNotFoundException e) {
                String error = "Entity not found: " + e.getMessage();
                errors.add(error);
                results.add(new BulkCreateResultDto.BookResult(
                        dto.getIsbn(), false, null, error));
                failedCount++;
                log.error("Entity not found for ISBN {}: {}", dto.getIsbn(), e.getMessage());
            } catch (Exception e) {
                String error = "Error creating book: " + e.getMessage();
                errors.add(error);
                results.add(new BulkCreateResultDto.BookResult(
                        dto.getIsbn(), false, null, error));
                failedCount++;
                log.error("Unexpected error for ISBN {}: {}", dto.getIsbn(), e.getMessage(), e);
            }
        }

        result.setResults(results);
        result.setTotalSuccess(successCount);
        result.setTotalFailed(failedCount);

        if (failedCount > 0) {
            String message = String.format(
                    "Partial success: %d saved, %d failed. Errors: %s",
                    successCount, failedCount, String.join("; ", errors));
            result.setMessage(message);
            log.warn("Bulk create completed with partial success: {} saved, {} failed", successCount, failedCount);
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }

        result.setMessage(String.format("Successfully created %d books", successCount));
        log.info("Bulk create WITHOUT transaction: {} successful, {} failed", successCount, failedCount);
        return result;
    }

    private void setBookRelations(Book book, BookDto dto) {
        if (dto.getPublisherId() != null) {
            Publisher publisher = publisherRepository.findById(dto.getPublisherId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Publisher not found: " + dto.getPublisherId()));
            book.setPublisher(publisher);
        }

        if (dto.getAuthorIds() != null && !dto.getAuthorIds().isEmpty()) {
            Set<Author> authors = new HashSet<>(authorRepository.findAllById(dto.getAuthorIds()));
            if (authors.size() != dto.getAuthorIds().size()) {
                throw new EntityNotFoundException("Some authors not found");
            }
            book.setAuthors(authors);
        }

        if (dto.getGenreIds() != null) {
            if (dto.getGenreIds().isEmpty()) {
                book.setGenres(new HashSet<>());
            } else {
                Set<Genre> genres = new HashSet<>(genreRepository.findAllById(dto.getGenreIds()));
                if (genres.size() != dto.getGenreIds().size()) {
                    throw new EntityNotFoundException("Some genres not found");
                }
                book.setGenres(genres);
            }
        }
    }
}