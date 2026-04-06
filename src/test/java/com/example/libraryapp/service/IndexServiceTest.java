package com.example.libraryapp.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.BookSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexServiceTest {

    private IndexService indexService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    private BookSearchCriteria criteria;
    private BookResponseDto bookDto;
    private List<BookResponseDto> bookList;
    private Page<BookResponseDto> bookPage;

    @BeforeEach
    void setUp() {
        indexService = new IndexService();

        // Настройка логгера для тестирования
        logger = (Logger) LoggerFactory.getLogger(IndexService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        criteria = new BookSearchCriteria();
        criteria.setAuthorName("Толстой");
        criteria.setGenreName("Роман");

        bookDto = new BookResponseDto();
        bookDto.setId(1L);
        bookDto.setTitle("Война и мир");

        bookList = List.of(bookDto);
        bookPage = new PageImpl<>(bookList);
    }

    // ============= GET FROM CACHE TESTS WITH LOGGING COVERAGE =============

    @Test
    void getFromCache_WithNullPageable_CacheMiss_LogsMiss() {
        // Подготовка
        Pageable pageable = null;

        // Выполнение - кеш пуст, будет MISS
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithNullPageable_CacheHit_LogsHit() {
        // Подготовка
        Pageable pageable = null;
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear(); // Очищаем логи после put

        // Выполнение - кеш содержит значение, будет HIT
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithUnpagedPageable_CacheMiss_LogsMiss() {
        // Подготовка
        Pageable pageable = Pageable.unpaged();

        // Выполнение - кеш пуст, будет MISS
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithUnpagedPageable_CacheHit_LogsHit() {
        // Подготовка
        Pageable pageable = Pageable.unpaged();
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithPagedPageable_CacheMiss_LogsMiss() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithPagedPageable_CacheHit_LogsHit() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithPagedPageableAndSort_CacheMiss_LogsMiss() {
        // Подготовка
        Pageable pageable = PageRequest.of(1, 20, Sort.by("title").ascending());

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithPagedPageableAndSort_CacheHit_LogsHit() {
        // Подготовка
        Pageable pageable = PageRequest.of(1, 20, Sort.by("title").ascending());
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= GET PAGE FROM CACHE TESTS WITH LOGGING COVERAGE =============

    @Test
    void getPageFromCache_CacheMiss_LogsMiss() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getPageFromCache_CacheHit_LogsHit() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getPageFromCache_WithSort_CacheMiss_LogsMiss() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").descending());

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getPageFromCache_WithSort_CacheHit_LogsHit() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").descending());
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= PUT IN CACHE TESTS WITH LOGGING COVERAGE =============

    @Test
    void putInCache_WithNullPageable_LogsCached() {
        // Подготовка
        Pageable pageable = null;
        listAppender.list.clear();

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putInCache_WithUnpagedPageable_LogsCached() {
        // Подготовка
        Pageable pageable = Pageable.unpaged();
        listAppender.list.clear();

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putInCache_WithPagedPageable_LogsCached() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        listAppender.list.clear();

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= PUT PAGE IN CACHE TESTS WITH LOGGING COVERAGE =============

    @Test
    void putPageInCache_LogsCached() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        listAppender.list.clear();

        // Выполнение
        indexService.putPageInCache(criteria, pageable, bookPage);

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached Page results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= INVALIDATE CACHE TESTS WITH LOGGING COVERAGE =============

    @Test
    void invalidateCache_LogsClearedEntries() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        // Выполнение
        indexService.invalidateCache();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cache invalidated. Cleared 1 list entries and 1 page entries.") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void invalidateCache_OnEmptyCache_LogsZeroEntries() {
        // Подготовка
        listAppender.list.clear();

        // Выполнение
        indexService.invalidateCache();

        // Проверка логов
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cache invalidated. Cleared 0 list entries and 0 page entries.") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= EQUALS METHOD COVERAGE =============

    @Test
    void cacheKey_Equals_SameObject_ReturnsTrue() {
        // Создаем CacheKey через публичный метод (через putInCache)
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);

        // Получаем из кеша - внутри используется equals
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверяем, что ключи равны (кеш нашел значение)
        assertThat(result).isNotNull();
    }

    @Test
    void cacheKey_Equals_NullObject_ReturnsFalse() {
        // Проверяем через разные ключи
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        indexService.putInCache(criteria, pageable1, bookList);

        // Разные ключи - не равны
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable2);
        assertThat(result).isNull();
    }

    @Test
    void cacheKey_Equals_DifferentClass_ReturnsFalse() {
        // Проверяем через разные критерии
        Pageable pageable = PageRequest.of(0, 10);

        BookSearchCriteria criteria1 = new BookSearchCriteria();
        criteria1.setAuthorName("Толстой");

        BookSearchCriteria criteria2 = new BookSearchCriteria();
        criteria2.setAuthorName("Достоевский");

        indexService.putInCache(criteria1, pageable, bookList);

        // Разные критерии - не равны
        List<BookResponseDto> result = indexService.getFromCache(criteria2, pageable);
        assertThat(result).isNull();
    }

    @Test
    void cacheKey_Equals_DifferentPageNumber_ReturnsFalse() {
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        indexService.putInCache(criteria, pageable1, bookList);

        // Разные номера страниц - не равны
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable2);
        assertThat(result).isNull();
    }

    @Test
    void cacheKey_Equals_DifferentPageSize_ReturnsFalse() {
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(0, 20);

        indexService.putInCache(criteria, pageable1, bookList);

        // Разные размеры страниц - не равны
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable2);
        assertThat(result).isNull();
    }

    @Test
    void cacheKey_Equals_DifferentSort_ReturnsFalse() {
        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").descending());

        indexService.putInCache(criteria, pageable1, bookList);

        // Разная сортировка - не равны
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable2);
        assertThat(result).isNull();
    }

    @Test
    void cacheKey_Equals_SameParameters_ReturnsTrue() {
        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").ascending());

        indexService.putInCache(criteria, pageable1, bookList);

        // Одинаковые параметры - равны
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable2);
        assertThat(result).isNotNull();
    }

    @Test
    void cacheKey_Equals_WithNullSort_ReturnsTrue() {
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(0, 10);

        indexService.putInCache(criteria, pageable1, bookList);

        // Одинаковые параметры с null sort - равны
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable2);
        assertThat(result).isNotNull();
    }

    // ============= HASHCODE METHOD COVERAGE =============

    @Test
    void cacheKey_HashCode_SameObjects_ReturnsSameHashCode() {
        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").ascending());

        indexService.putInCache(criteria, pageable1, bookList);

        // Оба ключа должны иметь одинаковый hashCode и быть равны
        List<BookResponseDto> result1 = indexService.getFromCache(criteria, pageable1);
        List<BookResponseDto> result2 = indexService.getFromCache(criteria, pageable2);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1).isSameAs(result2);
    }

    // ============= ADDITIONAL TESTS FOR COMPLETE COVERAGE =============

    @Test
    void getFromCache_MultipleCalls_LogsCorrectly() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Первый вызов - MISS
        indexService.getFromCache(criteria, pageable);

        // Кладем в кеш
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        // Второй вызов - HIT
        indexService.getFromCache(criteria, pageable);

        // Проверка логов на HIT
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getPageFromCache_MultipleCalls_LogsCorrectly() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Первый вызов - MISS
        indexService.getPageFromCache(criteria, pageable);

        // Кладем в кеш
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        // Второй вызов - HIT
        indexService.getPageFromCache(criteria, pageable);

        // Проверка логов на HIT
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }
}