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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.PageImpl;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

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


    private void setBookPublisher(Book book, Long publisherId) {
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Publisher not found with id: " + publisherId));
        book.setPublisher(publisher);
    }

    private void setBookAuthors(Book book, Set<Long> authorIds) {
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(authorIds));
        if (authors.size() != authorIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Some authors not found for ids: " + authorIds);
        }
        book.setAuthors(authors);
    }

    private void setBookGenres(Book book, Set<Long> genreIds) {
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(genreIds));
            if (genres.size() != genreIds.size()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Some genres not found for ids: " + genreIds);
            }
            book.setGenres(genres);
        }
    }

    private void invalidateCacheAndLog() {
        indexService.invalidateCache();
    }

    private Book createBookEntity(BookDto bookDto) {
        Book book = bookMapper.toEntity(bookDto);
        setBookPublisher(book, bookDto.getPublisherId());
        setBookAuthors(book, bookDto.getAuthorIds());
        setBookGenres(book, bookDto.getGenreIds());
        return book;
    }

    private Book createBookWithTempEntities(BookDto bookDto, boolean withTransaction) {
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
            String errorMsg = withTransaction
                    ? "Simulating error during save - transaction will rollback!"
                    : "Simulating error during save!";
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        return bookRepository.save(book);
    }


    private List<BookResponseDto> executeAndCache(
            BookSearchCriteria criteria,
            Pageable pageable,
            Function<BookSearchCriteria, List<Book>> queryExecutor) {

        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        if (cached != null) {
            return cached;
        }

        List<Book> books = queryExecutor.apply(criteria);
        List<BookResponseDto> results = books.stream()
                .map(bookMapper::toDto)
                .toList();

        indexService.putInCache(criteria, pageable, results);
        return results;
    }

    private Page<BookResponseDto> executeAndCachePage(
            BookSearchCriteria criteria,
            Pageable pageable,
            Function<BookSearchCriteria, Page<Book>> queryExecutor) {


        Page<BookResponseDto> cached = indexService.getPageFromCache(criteria, pageable);
        if (cached != null) {
            log.info("Page cache HIT for key: {}-{}-{}",
                    criteria.getCacheKey(),
                    pageable.getPageNumber(),
                    pageable.getPageSize());
            return cached;
        }

        log.info("Page cache MISS for key: {}-{}-{}",
                criteria.getCacheKey(),
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Book> page = queryExecutor.apply(criteria);
        Page<BookResponseDto> result = page.map(bookMapper::toDto);

        indexService.putPageInCache(criteria, pageable, result);

        return result;
    }

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

        return executeAndCachePage(criteria, pageable,
                c -> bookRepository.findBooksByComplexCriteriaWithPagination(
                        c.getAuthorName(),
                        c.getGenreName(),
                        c.getPublisherName(),
                        c.getMinPrice(),
                        c.getMaxPrice(),
                        c.getMinRating(),
                        pageable));
    }


    public List<BookResponseDto> searchBooksNative(BookSearchCriteria criteria) {
        log.info("Native search: {}", criteria);

        // Пытаемся получить из кэша
        List<BookResponseDto> cached = indexService.getFromCache(criteria, Pageable.unpaged());
        if (cached != null) {
            return cached;
        }

        // Выполняем native query, получаем Object[]
        List<Object[]> rows = bookRepository.findBooksByComplexCriteriaNative(
                criteria.getAuthorName(),
                criteria.getGenreName(),
                criteria.getPublisherName(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinRating());

        // Конвертируем Object[] в Book, потом в DTO
        List<BookResponseDto> results = rows.stream()
                .map(bookMapper::mapToBook)
                .map(bookMapper::toDto)
                .toList();

        // Сохраняем в кэш
        indexService.putInCache(criteria, Pageable.unpaged(), results);

        return results;
    }

    public Page<BookResponseDto> searchBooksNativeWithPagination(
            BookSearchCriteria criteria, Pageable pageable) {
        log.info("Native search with pagination: {}, page: {}, size: {}",
                criteria, pageable.getPageNumber(), pageable.getPageSize());

        // Получаем страницу Object[] из репозитория
        Page<Object[]> page = bookRepository.findBooksByComplexCriteriaNativeWithPagination(
                criteria.getAuthorName(),
                criteria.getGenreName(),
                criteria.getPublisherName(),
                criteria.getMinPrice(),
                criteria.getMaxPrice(),
                criteria.getMinRating(),
                pageable);

        // Конвертируем каждый Object[] в BookResponseDto
        List<BookResponseDto> content = page.getContent().stream()
                .map(bookMapper::mapToBook)
                .map(bookMapper::toDto)
                .toList();

        // Создаем новую страницу с DTO
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }


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


    @Transactional
    public BookResponseDto createBook(BookDto bookDto) {
        log.info("Creating new book: {}", bookDto.getTitle());

        // Проверка на дубликат ISBN
        if (bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Книга с ISBN " + bookDto.getIsbn() + " уже существует");
        }

        Book book = createBookEntity(bookDto);
        invalidateCacheAndLog();

        try {
            Book savedBook = bookRepository.save(book);
            log.info("Book created successfully with id: {}", savedBook.getId());
            return bookMapper.toDto(savedBook);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating book: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Не удалось создать книгу. Возможно, ISBN уже существует.");
        }
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

        // Проверка на дубликат ISBN при обновлении (если ISBN изменился)
        if (!book.getIsbn().equals(bookDto.getIsbn())
                && bookRepository.findByIsbn(bookDto.getIsbn()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Книга с ISBN " + bookDto.getIsbn() + " уже существует");
        }

        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setDescription(bookDto.getDescription());
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setPrice(bookDto.getPrice());

        setBookPublisher(book, bookDto.getPublisherId());
        setBookAuthors(book, bookDto.getAuthorIds());
        setBookGenres(book, bookDto.getGenreIds());

        invalidateCacheAndLog();

        try {
            Book updatedBook = bookRepository.save(book);
            log.info("Book updated successfully");
            return bookMapper.toDto(updatedBook);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating book: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Не удалось обновить книгу. Возможно, ISBN уже используется.");
        }
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
        invalidateCacheAndLog();
        log.info("Book deleted successfully");
    }

    public List<BookResponseDto> getAllBooksWithDetails() {
        log.info("Getting all books with details");
        return bookRepository.findAllWithDetails()
                .stream()
                .map(bookMapper::toDto)
                .toList();
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
                .toList();
    }

    public Book createBookWithoutTransaction(BookDto bookDto) {
        log.warn("DEMONSTRATING PARTIAL SAVE WITHOUT @Transactional");
        return createBookWithTempEntities(bookDto, false);
    }

    @Transactional
    public Book createBookWithTransaction(BookDto bookDto) {
        log.info("DEMONSTRATING ATOMIC SAVE WITH @Transactional");
        return createBookWithTempEntities(bookDto, true);
    }
}