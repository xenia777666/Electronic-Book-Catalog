package com.example.libraryapp.repository;

import com.example.libraryapp.domain.Book;
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

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByPublicationYear(Integer year);

    @Query("SELECT DISTINCT b FROM Book b " +
            "JOIN b.authors a " +
            "WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorName(@Param("authorName") String authorName);

    @Query("SELECT DISTINCT b FROM Book b JOIN b.genres g WHERE LOWER(g.name) = LOWER(:genreName)")
    List<Book> findByGenreName(@Param("genreName") String genreName);

    List<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    @EntityGraph(attributePaths = {"authors", "genres", "publisher"})
    @Query("SELECT DISTINCT b FROM Book b")
    List<Book> findAllWithDetails();

    @EntityGraph(attributePaths = {"authors", "genres", "publisher", "reviews"})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);
}