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

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCase(String title);

    List<Book> findByPublicationYear(Integer year);

    @Query("SELECT DISTINCT b FROM Book b "
            + "JOIN b.authors a "
            + "WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))")
    List<Book> findByAuthorName(@Param("authorName") String authorName);

    @Query("SELECT DISTINCT b FROM Book b "
            + "JOIN b.genres g "
            + "WHERE LOWER(g.name) = LOWER(:genreName)")
    List<Book> findByGenreName(@Param("genreName") String genreName);

    List<Book> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    // ============= ЛАБА 3: Сложный JPQL запрос =============
    @Query("SELECT DISTINCT b FROM Book b "
            + "LEFT JOIN b.authors a "
            + "LEFT JOIN b.genres g "
            + "LEFT JOIN b.publisher p "
            + "WHERE (:authorName IS NULL "
            + "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))) "
            + "AND (:genreName IS NULL "
            + "OR LOWER(g.name) = LOWER(:genreName)) "
            + "AND (:publisherName IS NULL "
            + "OR LOWER(p.name) = LOWER(:publisherName)) "
            + "AND (:minPrice IS NULL OR b.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR b.price <= :maxPrice) "
            + "AND (:minRating IS NULL OR b.averageRating >= :minRating)")
    List<Book> findBooksByComplexCriteria(
            @Param("authorName") String authorName,
            @Param("genreName") String genreName,
            @Param("publisherName") String publisherName,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating
    );

    // ============= ЛАБА 3: С пагинацией =============
    @Query("SELECT DISTINCT b FROM Book b "
            + "LEFT JOIN b.authors a "
            + "LEFT JOIN b.genres g "
            + "LEFT JOIN b.publisher p "
            + "WHERE (:authorName IS NULL "
            + "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))) "
            + "AND (:genreName IS NULL "
            + "OR LOWER(g.name) = LOWER(:genreName)) "
            + "AND (:publisherName IS NULL "
            + "OR LOWER(p.name) = LOWER(:publisherName)) "
            + "AND (:minPrice IS NULL OR b.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR b.price <= :maxPrice) "
            + "AND (:minRating IS NULL OR b.averageRating >= :minRating)")
    Page<Book> findBooksByComplexCriteriaWithPagination(
            @Param("authorName") String authorName,
            @Param("genreName") String genreName,
            @Param("publisherName") String publisherName,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

    // ============= ЛАБА 3: Native query =============
    @Query(value = "SELECT DISTINCT b.* FROM books b "
            + "LEFT JOIN book_author ba ON b.id = ba.book_id "
            + "LEFT JOIN authors a ON ba.author_id = a.id "
            + "LEFT JOIN book_genre bg ON b.id = bg.book_id "
            + "LEFT JOIN genres g ON bg.genre_id = g.id "
            + "LEFT JOIN publishers p ON b.publisher_id = p.id "
            + "WHERE (:authorName IS NULL OR :authorName = '' "
            + "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :authorName, '%'))) "
            + "AND (:genreName IS NULL OR :genreName = '' "
            + "OR LOWER(g.name) = LOWER(:genreName)) "
            + "AND (:publisherName IS NULL OR :publisherName = '' "
            + "OR LOWER(p.name) = LOWER(:publisherName)) "
            + "AND (:minPrice IS NULL OR b.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR b.price <= :maxPrice) "
            + "AND (:minRating IS NULL OR b.average_rating >= :minRating)",
            nativeQuery = true)
    List<Book> findBooksByComplexCriteriaNative(
            @Param("authorName") String authorName,
            @Param("genreName") String genreName,
            @Param("publisherName") String publisherName,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating
    );

    @EntityGraph(attributePaths = {"authors", "genres", "publisher"})
    @Query("SELECT DISTINCT b FROM Book b")
    List<Book> findAllWithDetails();

    @EntityGraph(attributePaths = {"authors", "genres", "publisher", "reviews"})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);
}