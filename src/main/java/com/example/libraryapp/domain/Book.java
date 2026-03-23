package com.example.libraryapp.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "average_rating")
    private Double averageRating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private Set<Author> authors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_genre",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // ============= Конструкторы =============
    public Book() {
    }

    public Book(Long id, String isbn, String title, String description,
                Integer publicationYear, BigDecimal price, Double averageRating,
                Publisher publisher, Set<Author> authors, Set<Genre> genres,
                List<Review> reviews) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.publicationYear = publicationYear;
        this.price = price;
        this.averageRating = averageRating;
        this.publisher = publisher;
        this.authors = authors;
        this.genres = genres;
        this.reviews = reviews;
    }

    // ============= Геттеры и сеттеры =============
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    // ============= ПРАВИЛЬНАЯ РЕАЛИЗАЦИЯ equals() =============
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Book book = (Book) o;
        // Исключаем коллекции, чтобы избежать циклических ссылок
        return Objects.equals(id, book.id)
                && Objects.equals(isbn, book.isbn)
                && Objects.equals(title, book.title)
                && Objects.equals(description, book.description)
                && Objects.equals(publicationYear, book.publicationYear)
                && Objects.equals(price, book.price)
                && Objects.equals(averageRating, book.averageRating)
                && Objects.equals(publisher, book.publisher);
    }

    // ============= ПРАВИЛЬНАЯ РЕАЛИЗАЦИЯ hashCode() =============
    @Override
    public int hashCode() {
        // Используем только поля, которые есть в equals()
        return Objects.hash(
                id,
                isbn,
                title,
                description,
                publicationYear,
                price,
                averageRating,
                publisher
        );
    }

    @Override
    public String toString() {
        return "Book{"
                + "id=" + id
                + ", isbn='" + isbn + '\''
                + ", title='" + title + '\''
                + ", publicationYear=" + publicationYear
                + ", price=" + price
                + ", averageRating=" + averageRating
                + '}';
    }
}