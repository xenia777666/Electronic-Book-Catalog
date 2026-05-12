package com.example.libraryapp.bootstrap;

import com.example.libraryapp.domain.Author;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.domain.Genre;
import com.example.libraryapp.domain.Publisher;
import com.example.libraryapp.domain.Review;
import com.example.libraryapp.repository.AuthorRepository;
import com.example.libraryapp.repository.BookRepository;
import com.example.libraryapp.repository.GenreRepository;
import com.example.libraryapp.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeedService {

    private static final String CITY_MOSCOW = "Москва";

    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookRepository bookRepository;

    private record BookSeed(
            String isbn,
            String title,
            String description,
            int year,
            BigDecimal price,
            double averageRating,
            Publisher publisher,
            Author author,
            Genre genre1,
            Genre genre2) {
    }

    @Transactional
    public void seedIfEmpty() {
        long existing = bookRepository.count();
        if (existing > 0) {
            log.info("Skipping demo seed: {} book(s) already in database.", existing);
            return;
        }

        Publisher ast = ensurePublisher("АСТ", CITY_MOSCOW, "+74951234567", "info@ast.ru");
        Publisher eksmo = ensurePublisher("Эксмо", CITY_MOSCOW, "+74959876543", "press@eksmo.ru");
        Publisher mif = ensurePublisher("МИФ", CITY_MOSCOW, "+74951112233", "hello@mann-ivanov-ferber.ru");

        Author tolstoy = ensureAuthor(
                "Лев Толстой",
                "Русский писатель и мыслитель.",
                LocalDate.of(1828, 9, 9));
        Author dostoevsky = ensureAuthor(
                "Фёдор Достоевский",
                "Русский писатель, философ.",
                LocalDate.of(1821, 11, 11));
        Author bulgakov = ensureAuthor(
                "Михаил Булгаков",
                "Русский писатель, драматург.",
                LocalDate.of(1891, 5, 15));
        Author orwell = ensureAuthor(
                "Джордж Оруэлл",
                "Британский писатель и журналист.",
                LocalDate.of(1903, 6, 25));
        Author rowling = ensureAuthor(
                "Джоан Роулинг",
                "Британская писательница.",
                LocalDate.of(1965, 7, 31));
        Author tolkien = ensureAuthor(
                "Дж. Р. Р. Толкин",
                "Британский писатель, филолог.",
                LocalDate.of(1892, 1, 3));
        Author christie = ensureAuthor(
                "Агата Кристи",
                "Английская писательница.",
                LocalDate.of(1890, 9, 15));
        Author kaverin = ensureAuthor(
                "Вениамин Каверин",
                "Советский писатель.",
                LocalDate.of(1902, 4, 19));

        Genre classic = ensureGenre("Классика", "Классическая литература");
        Genre roman = ensureGenre("Роман", "Художественная проза");
        Genre fantasy = ensureGenre("Фэнтези", "Жанр фэнтези");
        Genre sciFi = ensureGenre("Фантастика", "Научная и социальная фантастика");
        Genre detective = ensureGenre("Детектив", "Детективная литература");
        Genre adventure = ensureGenre("Приключения", "Приключенческая литература");

        Book b1 = buildBook(new BookSeed(
                "9785010101001",
                "Война и мир",
                "Эпопея о русском обществе в эпоху войн 1812 года.",
                1869,
                new BigDecimal("899.00"),
                4.7,
                ast,
                tolstoy,
                classic,
                roman));
        addReview(b1, "Мария", 5, "Масштабное произведение, перечитываю с удовольствием.");

        Book b2 = buildBook(new BookSeed(
                "9785010101002",
                "Преступление и наказание",
                "Психологический роман о студенте Раскольникове.",
                1866,
                new BigDecimal("550.00"),
                4.8,
                ast,
                dostoevsky,
                classic,
                roman));
        addReview(b2, "Алексей", 5, "Сильная книга, держит в напряжении.");

        Book b3 = buildBook(new BookSeed(
                "9785010101003",
                "Идиот",
                "Роман о князе Мышкине.",
                1869,
                new BigDecimal("520.00"),
                4.6,
                ast,
                dostoevsky,
                classic,
                roman));

        Book b4 = buildBook(new BookSeed(
                "9785010101004",
                "Мастер и Маргарита",
                "Философско-сатирический роман.",
                1967,
                new BigDecimal("480.00"),
                4.9,
                eksmo,
                bulgakov,
                roman,
                fantasy));
        addReview(b4, "Ольга", 5, "Фирменный юмор и глубина.");

        Book b5 = buildBook(new BookSeed(
                "9785010101005",
                "Собачье сердце",
                "Повесть-сатира.",
                1925,
                new BigDecimal("390.00"),
                4.5,
                eksmo,
                bulgakov,
                roman,
                sciFi));

        Book b6 = buildBook(new BookSeed(
                "9785010101006",
                "1984",
                "Антиутопия о тоталитарном обществе.",
                1949,
                new BigDecimal("450.00"),
                4.8,
                eksmo,
                orwell,
                sciFi,
                roman));
        addReview(b6, "Дмитрий", 4, "Актуально и по сей день.");

        Book b7 = buildBook(new BookSeed(
                "9785010101007",
                "Скотный двор",
                "Аллегорическая повесть.",
                1945,
                new BigDecimal("420.00"),
                4.6,
                eksmo,
                orwell,
                sciFi,
                roman));

        Book b8 = buildBook(new BookSeed(
                "9785010101008",
                "Гарри Поттер и философский камень",
                "Первый роман о юном волшебнике.",
                1997,
                new BigDecimal("790.00"),
                4.9,
                mif,
                rowling,
                fantasy,
                adventure));
        addReview(b8, "Елена", 5, "Детям и взрослым — отличное начало серии.");

        Book b9 = buildBook(new BookSeed(
                "9785010101009",
                "Хоббит, или Туда и обратно",
                "Сказочное путешествие Бильбо Бэггинса.",
                1937,
                new BigDecimal("680.00"),
                4.8,
                mif,
                tolkien,
                fantasy,
                adventure));

        Book b10 = buildBook(new BookSeed(
                "9785010101010",
                "Десять негритят",
                "Классический детективный роман.",
                1939,
                new BigDecimal("410.00"),
                4.7,
                eksmo,
                christie,
                detective,
                roman));

        Book b11 = buildBook(new BookSeed(
                "9785010101011",
                "Два капитана",
                "Приключенческий роман для юношества.",
                1938,
                new BigDecimal("360.00"),
                4.5,
                ast,
                kaverin,
                adventure,
                roman));
        addReview(b11, "Сергей", 4, "Читал в школе — помню до сих пор.");

        bookRepository.save(b1);
        bookRepository.save(b2);
        bookRepository.save(b3);
        bookRepository.save(b4);
        bookRepository.save(b5);
        bookRepository.save(b6);
        bookRepository.save(b7);
        bookRepository.save(b8);
        bookRepository.save(b9);
        bookRepository.save(b10);
        bookRepository.save(b11);

        log.info("Demo seed completed: inserted 11 books with publishers, authors, genres and selected reviews.");
    }

    private Publisher ensurePublisher(String name, String address, String phone, String email) {
        return publisherRepository.findByName(name).orElseGet(() -> {
            Publisher p = new Publisher();
            p.setName(name);
            p.setAddress(address);
            p.setPhone(phone);
            p.setEmail(email);
            return publisherRepository.save(p);
        });
    }

    private Author ensureAuthor(String name, String biography, LocalDate birthDate) {
        return authorRepository.findByName(name).orElseGet(() -> {
            Author a = new Author();
            a.setName(name);
            a.setBiography(biography);
            a.setBirthDate(birthDate);
            return authorRepository.save(a);
        });
    }

    private Genre ensureGenre(String name, String description) {
        return genreRepository.findByName(name).orElseGet(() -> {
            Genre g = new Genre();
            g.setName(name);
            g.setDescription(description);
            return genreRepository.save(g);
        });
    }

    private Book buildBook(BookSeed s) {
        Book b = new Book();
        b.setIsbn(s.isbn());
        b.setTitle(s.title());
        b.setDescription(s.description());
        b.setPublicationYear(s.year());
        b.setPrice(s.price());
        b.setAverageRating(s.averageRating());
        b.setPublisher(s.publisher());
        b.getAuthors().add(s.author());
        b.getGenres().add(s.genre1());
        b.getGenres().add(s.genre2());
        return b;
    }

    private void addReview(Book book, String reviewerName, int rating, String comment) {
        Review r = new Review();
        r.setReviewerName(reviewerName);
        r.setRating(rating);
        r.setComment(comment);
        r.setBook(book);
        book.getReviews().add(r);
    }
}
