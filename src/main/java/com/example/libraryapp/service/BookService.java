package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookDto;
import com.example.libraryapp.api.dto.BookResponseDto;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final GenreRepository genreRepository;
    private final BookMapper bookMapper;

    // ==================== ПУБЛИЧНЫЕ МЕТОДЫ ====================

    @Transactional
    public BookResponseDto createBook(BookDto bookDto) {
        log.info("Creating new book: {}", bookDto.getTitle());
        Book book = bookMapper.toEntity(bookDto);

        setBookPublisher(book, bookDto.getPublisherId());
        setBookAuthors(book, bookDto.getAuthorIds());
        setBookGenres(book, bookDto.getGenreIds());

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
        return bookMapper.toDto(findBookById(id));
    }

    @Transactional
    public BookResponseDto updateBook(Long id, BookDto bookDto) {
        log.info("Updating book with id: {}", id);
        Book book = findBookById(id);

        updateBookFields(book, bookDto);
        setBookPublisher(book, bookDto.getPublisherId());
        setBookAuthors(book, bookDto.getAuthorIds());
        setBookGenres(book, bookDto.getGenreIds());

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

    public List<BookResponseDto> findBooksByAuthor(String authorName) {
        log.debug("Searching books by author: {}", authorName);
        return bookRepository.findByAuthorName(authorName)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public List<BookResponseDto> findBooksByGenre(String genreName) {
        log.debug("Searching books by genre: {}", genreName);
        return bookRepository.findByGenreName(genreName)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public List<BookResponseDto> findBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Searching books by price range: {} - {}", minPrice, maxPrice);
        return bookRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    public List<BookResponseDto> getAllBooksWithDetails() {
        log.info("Getting all books with details (authors, genres, publisher)");
        return bookRepository.findAllWithDetails()
                .stream()
                .map(bookMapper::toDto)
                .toList();
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
                .toList();
    }

    public Book createBookWithoutTransaction(BookDto bookDto) {
        log.warn("DEMONSTRATING PARTIAL SAVE WITHOUT @Transactional");
        return saveBookWithTempEntities(bookDto);
    }

    @Transactional
    public Book createBookWithTransaction(BookDto bookDto) {
        log.info("DEMONSTRATING ATOMIC SAVE WITH @Transactional");
        return saveBookWithTempEntities(bookDto);
    }

    // ==================== ПРИВАТНЫЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
    }

    private void setBookPublisher(Book book, Long publisherId) {
        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Publisher not found with id: " + publisherId
                ));
        book.setPublisher(publisher);
    }

    private void setBookAuthors(Book book, Set<Long> authorIds) {
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(authorIds));
        if (authors.size() != authorIds.size()) {
            throw new EntityNotFoundException("Some authors not found for ids: " + authorIds);
        }
        book.setAuthors(authors);
    }

    private void setBookGenres(Book book, Set<Long> genreIds) {
        if (genreIds != null && !genreIds.isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(genreIds));
            if (genres.size() != genreIds.size()) {
                throw new EntityNotFoundException("Some genres not found for ids: " + genreIds);
            }
            book.setGenres(genres);
        }
    }

    private void updateBookFields(Book book, BookDto bookDto) {
        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setDescription(bookDto.getDescription());
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setPrice(bookDto.getPrice());
    }

    private Book saveBookWithTempEntities(BookDto bookDto) {
        Publisher tempPublisher = createAndSaveTempPublisher();
        Author tempAuthor = createAndSaveTempAuthor();

        Book book = bookMapper.toEntity(bookDto);
        book.setPublisher(tempPublisher);
        book.getAuthors().add(tempAuthor);

        if (bookDto.getTitle() != null && bookDto.getTitle().contains("error")) {
            log.error("Simulating error during save!");
            throw new IllegalStateException("Simulated error during book creation");
        }

        return bookRepository.save(book);
    }

    private Publisher createAndSaveTempPublisher() {
        Publisher tempPublisher = new Publisher();
        tempPublisher.setName("TEMP_" + System.currentTimeMillis());
        return publisherRepository.save(tempPublisher);
    }

    private Author createAndSaveTempAuthor() {
        Author tempAuthor = new Author();
        tempAuthor.setName("TEMP_AUTHOR_" + System.currentTimeMillis());
        return authorRepository.save(tempAuthor);
    }
}