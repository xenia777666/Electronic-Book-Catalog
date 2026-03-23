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

    @Query("SELECT DISTINCT b FROM Book b "
            + "LEFT JOIN b.authors a "
            + "LEFT JOIN b.genres g "
            + "LEFT JOIN b.publisher p "
            + "WHERE (:authorName IS NULL "
            + "OR CAST(a.name AS string) LIKE CONCAT('%', CAST(:authorName AS string), '%')) "
            + "AND (:genreName IS NULL OR CAST(g.name AS string) = CAST(:genreName AS string)) "
            + "AND (:publisherName IS NULL OR CAST(p.name AS string) = CAST(:publisherName AS string)) "
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

    @Query("SELECT DISTINCT b FROM Book b "
            + "LEFT JOIN b.authors a "
            + "LEFT JOIN b.genres g "
            + "LEFT JOIN b.publisher p "
            + "WHERE (:authorName IS NULL "
            + "OR CAST(a.name AS string) LIKE CONCAT('%', CAST(:authorName AS string), '%')) "
            + "AND (:genreName IS NULL OR CAST(g.name AS string) = CAST(:genreName AS string)) "
            + "AND (:publisherName IS NULL OR CAST(p.name AS string) = CAST(:publisherName AS string)) "
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

    @Query(value = "SELECT DISTINCT b.* FROM books b "
            + "LEFT JOIN book_author ba ON b.id = ba.book_id "
            + "LEFT JOIN authors a ON ba.author_id = a.id "
            + "LEFT JOIN book_genre bg ON b.id = bg.book_id "
            + "LEFT JOIN genres g ON bg.genre_id = g.id "
            + "LEFT JOIN publishers p ON b.publisher_id = p.id "
            + "WHERE (CAST(:authorName AS text) IS NULL OR CAST(:authorName AS text) = '' "
            + "OR a.name ILIKE CONCAT('%', CAST(:authorName AS text), '%')) "
            + "AND (CAST(:genreName AS text) IS NULL OR CAST(:genreName AS text) = '' "
            + "OR g.name ILIKE CAST(:genreName AS text)) "
            + "AND (CAST(:publisherName AS text) IS NULL OR CAST(:publisherName AS text) = '' "
            + "OR p.name ILIKE CAST(:publisherName AS text)) "
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

    @Query(value = "SELECT DISTINCT b.* FROM books b "
            + "LEFT JOIN book_author ba ON b.id = ba.book_id "
            + "LEFT JOIN authors a ON ba.author_id = a.id "
            + "LEFT JOIN book_genre bg ON b.id = bg.book_id "
            + "LEFT JOIN genres g ON bg.genre_id = g.id "
            + "LEFT JOIN publishers p ON b.publisher_id = p.id "
            + "WHERE (CAST(:authorName AS text) IS NULL OR CAST(:authorName AS text) = '' "
            + "OR a.name ILIKE CONCAT('%', CAST(:authorName AS text), '%')) "
            + "AND (CAST(:genreName AS text) IS NULL OR CAST(:genreName AS text) = '' "
            + "OR g.name ILIKE CAST(:genreName AS text)) "
            + "AND (CAST(:publisherName AS text) IS NULL OR CAST(:publisherName AS text) = '' "
            + "OR p.name ILIKE CAST(:publisherName AS text)) "
            + "AND (:minPrice IS NULL OR b.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR b.price <= :maxPrice) "
            + "AND (:minRating IS NULL OR b.average_rating >= :minRating)",
            countQuery = "SELECT COUNT(DISTINCT b.id) FROM books b "
                    + "LEFT JOIN book_author ba ON b.id = ba.book_id "
                    + "LEFT JOIN authors a ON ba.author_id = a.id "
                    + "LEFT JOIN book_genre bg ON b.id = bg.book_id "
                    + "LEFT JOIN genres g ON bg.genre_id = g.id "
                    + "LEFT JOIN publishers p ON b.publisher_id = p.id "
                    + "WHERE (CAST(:authorName AS text) IS NULL OR CAST(:authorName AS text) = '' "
                    + "OR a.name ILIKE CONCAT('%', CAST(:authorName AS text), '%')) "
                    + "AND (CAST(:genreName AS text) IS NULL OR CAST(:genreName AS text) = '' "
                    + "OR g.name ILIKE CAST(:genreName AS text)) "
                    + "AND (CAST(:publisherName AS text) IS NULL OR CAST(:publisherName AS text) = '' "
                    + "OR p.name ILIKE CAST(:publisherName AS text)) "
                    + "AND (:minPrice IS NULL OR b.price >= :minPrice) "
                    + "AND (:maxPrice IS NULL OR b.price <= :maxPrice) "
                    + "AND (:minRating IS NULL OR b.average_rating >= :minRating)",
            nativeQuery = true)
    Page<Book> findBooksByComplexCriteriaNativeWithPagination(
            @Param("authorName") String authorName,
            @Param("genreName") String genreName,
            @Param("publisherName") String publisherName,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"authors", "genres", "publisher"})
    @Query("SELECT DISTINCT b FROM Book b")
    List<Book> findAllWithDetails();

    @EntityGraph(attributePaths = {"authors", "genres", "publisher", "reviews"})
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdWithDetails(@Param("id") Long id);
}