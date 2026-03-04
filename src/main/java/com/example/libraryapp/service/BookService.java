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

    // CREATE
    @Transactional
    public BookResponseDto createBook(BookDto bookDto) {
        log.info("Creating new book: {}", bookDto.getTitle());

        Book book = bookMapper.toEntity(bookDto);

        // Устанавливаем издателя
        Publisher publisher = publisherRepository.findById(bookDto.getPublisherId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Publisher not found with id: " + bookDto.getPublisherId()
                ));
        book.setPublisher(publisher);

        // Устанавливаем авторов
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(bookDto.getAuthorIds()));
        if (authors.size() != bookDto.getAuthorIds().size()) {
            throw new EntityNotFoundException("Some authors not found");
        }
        book.setAuthors(authors);

        // Устанавливаем жанры (если есть)
        if (bookDto.getGenreIds() != null && !bookDto.getGenreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(bookDto.getGenreIds()));
            book.setGenres(genres);
        }

        Book savedBook = bookRepository.save(book);
        log.info("Book created successfully with id: {}", savedBook.getId());

        return bookMapper.toDto(savedBook);
    }

    // READ all with pagination
    public Page<BookResponseDto> getAllBooks(Pageable pageable) {
        log.debug("Getting all books with pagination: page {}, size {}",
                pageable.getPageNumber(), pageable.getPageSize());
        return bookRepository.findAll(pageable)
                .map(bookMapper::toDto);
    }

    // READ one
    public BookResponseDto getBookById(Long id) {
        log.debug("Getting book by id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));
        return bookMapper.toDto(book);
    }

    // UPDATE
    @Transactional
    public BookResponseDto updateBook(Long id, BookDto bookDto) {
        log.info("Updating book with id: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book not found with id: " + id));

        // Обновляем поля
        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setDescription(bookDto.getDescription());
        book.setPublicationYear(bookDto.getPublicationYear());
        book.setPrice(bookDto.getPrice());

        // Обновляем издателя
        Publisher publisher = publisherRepository.findById(bookDto.getPublisherId())
                .orElseThrow(() -> new EntityNotFoundException("Publisher not found"));
        book.setPublisher(publisher);

        // Обновляем авторов
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(bookDto.getAuthorIds()));
        book.setAuthors(authors);

        // Обновляем жанры
        if (bookDto.getGenreIds() != null) {
            Set<Genre> genres = new HashSet<>(genreRepository.findAllById(bookDto.getGenreIds()));
            book.setGenres(genres);
        }

        Book updatedBook = bookRepository.save(book);
        log.info("Book updated successfully");

        return bookMapper.toDto(updatedBook);
    }

    // DELETE
    @Transactional
    public void deleteBook(Long id) {
        log.info("Deleting book with id: {}", id);

        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);

        log.info("Book deleted successfully");
    }

    // Поиск по автору
    public List<BookResponseDto> findBooksByAuthor(String authorName) {
        log.debug("Searching books by author: {}", authorName);
        return bookRepository.findByAuthorName(authorName)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    // Поиск по жанру
    public List<BookResponseDto> findBooksByGenre(String genreName) {
        log.debug("Searching books by genre: {}", genreName);
        return bookRepository.findByGenreName(genreName)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    // Поиск по цене
    public List<BookResponseDto> findBooksByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Searching books by price range: {} - {}", minPrice, maxPrice);
        return bookRepository.findByPriceBetween(minPrice, maxPrice)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    // READ with details (решение N+1)
    public List<BookResponseDto> getAllBooksWithDetails() {
        log.info("Getting all books with details (authors, genres, publisher)");

        // Используем метод с EntityGraph - 1 запрос вместо N+1
        List<Book> books = bookRepository.findAllWithDetails();

        log.debug("Loaded {} books with details", books.size());
        return books.stream()
                .map(bookMapper::toDto)
                .toList();
    }

    // Демонстрация проблемы N+1
    public List<BookResponseDto> getAllBooksWithNPlus1Problem() {
        log.warn("DEMONSTRATING N+1 PROBLEM");

        // 1 запрос на получение книг
        List<Book> books = bookRepository.findAll();

        // Для каждой книги дополнительные запросы к авторам и жанрам (N запросов)
        for (Book book : books) {
            log.debug("Loading authors for book {}: will cause additional query", book.getId());
            book.getAuthors().size(); // Триггер для загрузки LAZY коллекции

            log.debug("Loading genres for book {}: will cause additional query", book.getId());
            book.getGenres().size(); // Еще один запрос
        }

        return books.stream()
                .map(bookMapper::toDto)
                .toList();
    }

    // Метод без @Transactional - демонстрация частичного сохранения
    public Book createBookWithoutTransaction(BookDto bookDto) {
        log.warn("DEMONSTRATING PARTIAL SAVE WITHOUT @Transactional");

        // Сначала создаем временного издателя (сохранится!)
        Publisher tempPublisher = new Publisher();
        tempPublisher.setName("TEMP_" + System.currentTimeMillis());
        tempPublisher = publisherRepository.save(tempPublisher);
        log.info("Temporary publisher saved with id: {}", tempPublisher.getId());

        // Создаем временного автора (сохранится!)
        Author tempAuthor = new Author();
        tempAuthor.setName("TEMP_AUTHOR_" + System.currentTimeMillis());
        tempAuthor = authorRepository.save(tempAuthor);
        log.info("Temporary author saved with id: {}", tempAuthor.getId());

        // Создаем книгу
        Book book = bookMapper.toEntity(bookDto);
        book.setPublisher(tempPublisher);
        book.getAuthors().add(tempAuthor);

        // Имитируем ошибку
        if (bookDto.getTitle() != null && bookDto.getTitle().contains("error")) {
            log.error("Simulating error during save!");
            throw new IllegalStateException("Simulated error during book creation");
        }

        Book savedBook = bookRepository.save(book);
        log.info("Book saved successfully");
        return savedBook;
    }

    // Метод С @Transactional - полный откат при ошибке
    @Transactional
    public Book createBookWithTransaction(BookDto bookDto) {
        log.info("DEMONSTRATING ATOMIC SAVE WITH @Transactional");

        // Создаем временного издателя
        Publisher tempPublisher = new Publisher();
        tempPublisher.setName("TEMP_" + System.currentTimeMillis());
        tempPublisher = publisherRepository.save(tempPublisher);
        log.info("Temporary publisher saved with id: {}", tempPublisher.getId());

        // Создаем временного автора
        Author tempAuthor = new Author();
        tempAuthor.setName("TEMP_AUTHOR_" + System.currentTimeMillis());
        tempAuthor = authorRepository.save(tempAuthor);
        log.info("Temporary author saved with id: {}", tempAuthor.getId());

        // Создаем книгу
        Book book = bookMapper.toEntity(bookDto);
        book.setPublisher(tempPublisher);
        book.getAuthors().add(tempAuthor);

        // Имитируем ошибку
        if (bookDto.getTitle() != null && bookDto.getTitle().contains("error")) {
            log.error("Simulating error during save - transaction will rollback!");
            throw new IllegalStateException("Simulated error during book creation");
        }

        Book savedBook = bookRepository.save(book);
        log.info("Book saved successfully");
        return savedBook;
    }
}