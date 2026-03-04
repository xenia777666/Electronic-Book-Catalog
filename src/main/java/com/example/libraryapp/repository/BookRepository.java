package com.example.libraryapp.repository;

import com.example.libraryapp.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Поиск по ISBN
    Optional<Book> findByIsbn(String isbn);

    // Поиск по названию (частичное совпадение, без учета регистра)
    List<Book> findByTitleContainingIgnoreCase(String title);

    // Поиск по году публикации
    List<Book> findByPublicationYear(Integer year);

    // Поиск по имени автора
    @Query("SELECT DISTINCT b FROM Book b JOIN b.authors a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorName(@Param("authorName") String authorName);

    // Поиск по названию жанра
    @Query("SELECT DISTINCT b FROM Book b JOIN b.genres g WHERE LOWER(g.name) = LOWER(:genreName)")
    List<Book> findByGenreName(@Param("genreName") String genreName);

    // Поиск книг с ценой в диапазоне
    List<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // Пагинация с сортировкой
    Page<Book> findAllByOrderByTitleAsc(Pageable pageable);

    // Решение N+1 через EntityGraph
    @EntityGraph(attributePaths = {"authors", "genres", "publisher"})
    @Query("SELECT DISTINCT b FROM Book b")
    List<Book> findAllWithDetails();

    // Решение N+1 через JOIN FETCH
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN FETCH b.authors " +
            "LEFT JOIN FETCH b.genres " +
            "LEFT JOIN FETCH b.publisher")
    List<Book> findAllWithDetailsJoinFetch();

    // Для конкретной книги с деталями
    @EntityGraph(attributePaths = {"authors", "genres", "publisher", "reviews"})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);
}