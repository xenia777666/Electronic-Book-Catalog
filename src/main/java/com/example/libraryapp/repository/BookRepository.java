package com.example.libraryapp.repository;

import com.example.libraryapp.domain.Book;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class BookRepository {

    private final List<Book> books = new ArrayList<>();

    @PostConstruct
    public void init() {
        books.add(new Book(1L, "978-3-16-148410-0", "Война и мир", "Лев Толстой", 1869, "Роман"));
        books.add(new Book(2L, "978-0-14-044913-6", "Преступление и наказание", "Фёдор Достоевский", 1866, "Роман"));
        books.add(new Book(3L, "978-5-699-18031-2", "Мастер и Маргарита", "Михаил Булгаков", 1967, "Роман"));
    }

    public List<Book> findAll() {
        return new ArrayList<>(books);
    }

    public Optional<Book> findById(Long id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst();
    }

    public List<Book> findByAuthor(String author) {
        return books.stream()
                .filter(book -> book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .toList();
    }
}