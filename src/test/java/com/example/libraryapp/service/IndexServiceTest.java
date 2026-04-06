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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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

    // ============= TESTS FOR getCacheSize METHOD =============

    @Test
    void getCacheSize_EmptyCache_ReturnsZero() {
        // Проверка
        assertThat(indexService.getCacheSize()).isZero();
    }

    @Test
    void getCacheSize_WithOnlyListEntries_ReturnsCorrectSize() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        indexService.putInCache(criteria, pageable1, bookList);
        indexService.putInCache(criteria, pageable2, bookList);

        // Проверка
        assertThat(indexService.getCacheSize()).isEqualTo(2);
    }

    @Test
    void getCacheSize_WithOnlyPageEntries_ReturnsCorrectSize() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        indexService.putPageInCache(criteria, pageable1, bookPage);
        indexService.putPageInCache(criteria, pageable2, bookPage);

        // Проверка
        assertThat(indexService.getCacheSize()).isEqualTo(2);
    }

    @Test
    void getCacheSize_WithMixedEntries_ReturnsCorrectSize() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        indexService.putInCache(criteria, pageable1, bookList);
        indexService.putPageInCache(criteria, pageable2, bookPage);

        // Проверка
        assertThat(indexService.getCacheSize()).isEqualTo(2);
    }

    @Test
    void getCacheSize_AfterInvalidate_ReturnsZero() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        indexService.putPageInCache(criteria, pageable, bookPage);

        assertThat(indexService.getCacheSize()).isEqualTo(2);

        // Выполнение
        indexService.invalidateCache();

        // Проверка
        assertThat(indexService.getCacheSize()).isZero();
    }

    // ============= TESTS FOR CacheKey EQUALS METHOD USING REFLECTION =============

    @Test
    void cacheKey_Equals_SameObject_ReturnsTrue() throws Exception {
        // Создаем CacheKey через рефлексию
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Object key1 = constructor.newInstance(criteria, pageable);

        // Вызываем equals для одного и того же объекта
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        // Проверка: this == o
        boolean result = (boolean) equalsMethod.invoke(key1, key1);
        assertThat(result).isTrue();
    }

    @Test
    void cacheKey_Equals_NullObject_ReturnsFalse() throws Exception {
        // Создаем CacheKey через рефлексию
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable = PageRequest.of(0, 10);
        Object key1 = constructor.newInstance(criteria, pageable);

        // Вызываем equals с null
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        // Проверка: o == null
        boolean result = (boolean) equalsMethod.invoke(key1, new Object[]{null});
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_DifferentClass_ReturnsFalse() throws Exception {
        // Создаем CacheKey через рефлексию
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable = PageRequest.of(0, 10);
        Object key1 = constructor.newInstance(criteria, pageable);

        // Вызываем equals с объектом другого класса
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        String differentObject = "test";
        boolean result = (boolean) equalsMethod.invoke(key1, differentObject);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_SamePageNumberAndPageSize_ReturnsTrue() throws Exception {
        // Создаем два одинаковых CacheKey
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor1 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor1.setAccessible(true);
        Constructor<?> constructor2 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor2.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(0, 10);

        Object key1 = constructor1.newInstance(criteria, pageable1);
        Object key2 = constructor2.newInstance(criteria, pageable2);

        // Вызываем equals
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isTrue();
    }

    @Test
    void cacheKey_Equals_DifferentPageNumber_ReturnsFalse() throws Exception {
        // Создаем два разных CacheKey
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor1 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor1.setAccessible(true);
        Constructor<?> constructor2 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor2.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        Object key1 = constructor1.newInstance(criteria, pageable1);
        Object key2 = constructor2.newInstance(criteria, pageable2);

        // Вызываем equals
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_DifferentPageSize_ReturnsFalse() throws Exception {
        // Создаем два разных CacheKey
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor1 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor1.setAccessible(true);
        Constructor<?> constructor2 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor2.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(0, 20);

        Object key1 = constructor1.newInstance(criteria, pageable1);
        Object key2 = constructor2.newInstance(criteria, pageable2);

        // Вызываем equals
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_DifferentCriteria_ReturnsFalse() throws Exception {
        // Создаем два разных CacheKey
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor1 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor1.setAccessible(true);
        Constructor<?> constructor2 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor2.setAccessible(true);

        BookSearchCriteria criteria2 = new BookSearchCriteria();
        criteria2.setAuthorName("Достоевский");

        Pageable pageable = PageRequest.of(0, 10);

        Object key1 = constructor1.newInstance(criteria, pageable);
        Object key2 = constructor2.newInstance(criteria2, pageable);

        // Вызываем equals
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_DifferentSort_ReturnsFalse() throws Exception {
        // Создаем два разных CacheKey
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor1 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor1.setAccessible(true);
        Constructor<?> constructor2 = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor2.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").descending());

        Object key1 = constructor1.newInstance(criteria, pageable1);
        Object key2 = constructor2.newInstance(criteria, pageable2);

        // Вызываем equals
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_WithNullSort_ReturnsTrue() throws Exception {
        // Создаем два одинаковых CacheKey с null sort
        Class<?> cacheKeyClass = getCacheKeyClass();

        // Используем конструктор с параметрами int, int, String
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, int.class, int.class, String.class);
        constructor.setAccessible(true);

        Object key1 = constructor.newInstance(criteria, 0, 10, null);
        Object key2 = constructor.newInstance(criteria, 0, 10, null);

        // Вызываем equals
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isTrue();
    }

    @Test
    void cacheKey_Equals_WithNullCriteria_ReturnsTrue() throws Exception {
        // Создаем два одинаковых CacheKey с null criteria
        Class<?> cacheKeyClass = getCacheKeyClass();

        // Используем конструктор с параметрами int, int, String
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, int.class, int.class, String.class);
        constructor.setAccessible(true);

        Object key1 = constructor.newInstance(null, 0, 10, "title: ASC");
        Object key2 = constructor.newInstance(null, 0, 10, "title: ASC");

        // Вызываем equals
        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isTrue();
    }

    // ============= TESTS FOR CacheKey HASHCODE METHOD =============

    @Test
    void cacheKey_HashCode_EqualObjects_ReturnsSameHashCode() throws Exception {
        // Создаем два одинаковых CacheKey
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").ascending());

        Object key1 = constructor.newInstance(criteria, pageable1);
        Object key2 = constructor.newInstance(criteria, pageable2);

        // Вызываем hashCode
        Method hashCodeMethod = cacheKeyClass.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);

        int hashCode1 = (int) hashCodeMethod.invoke(key1);
        int hashCode2 = (int) hashCodeMethod.invoke(key2);

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    void cacheKey_HashCode_DifferentObjects_ReturnsDifferentHashCode() throws Exception {
        // Создаем два разных CacheKey
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        Object key1 = constructor.newInstance(criteria, pageable1);
        Object key2 = constructor.newInstance(criteria, pageable2);

        // Вызываем hashCode
        Method hashCodeMethod = cacheKeyClass.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);

        int hashCode1 = (int) hashCodeMethod.invoke(key1);
        int hashCode2 = (int) hashCodeMethod.invoke(key2);

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    // ============= TESTS FOR CacheKey TOSTRING METHOD =============

    @Test
    void cacheKey_ToString_ReturnsCorrectFormat() throws Exception {
        // Создаем CacheKey
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        Object key = constructor.newInstance(criteria, pageable);

        // Вызываем toString
        Method toStringMethod = cacheKeyClass.getDeclaredMethod("toString");
        toStringMethod.setAccessible(true);

        String result = (String) toStringMethod.invoke(key);

        assertThat(result).contains(criteria.getCacheKey());
        assertThat(result).contains("0");
        assertThat(result).contains("10");
    }

    @Test
    void cacheKey_ToString_WithNullSort_ReturnsCorrectFormat() throws Exception {
        // Создаем CacheKey с null sort
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, int.class, int.class, String.class);
        constructor.setAccessible(true);

        Object key = constructor.newInstance(criteria, 0, 10, null);

        // Вызываем toString
        Method toStringMethod = cacheKeyClass.getDeclaredMethod("toString");
        toStringMethod.setAccessible(true);

        String result = (String) toStringMethod.invoke(key);

        assertThat(result).contains(criteria.getCacheKey());
        assertThat(result).contains("0");
        assertThat(result).contains("10");
        assertThat(result).contains("unsorted");
    }

    // ============= HELPER METHODS =============

    private Class<?> getCacheKeyClass() throws Exception {
        Class<?>[] declaredClasses = IndexService.class.getDeclaredClasses();
        for (Class<?> declaredClass : declaredClasses) {
            if (declaredClass.getSimpleName().equals("CacheKey")) {
                return declaredClass;
            }
        }
        throw new RuntimeException("CacheKey class not found");
    }

    // ============= REMAINING TESTS FOR COMPLETE COVERAGE =============

    @Test
    void getFromCache_WithNullPageable_CacheMiss_LogsMiss() {
        Pageable pageable = null;
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        assertThat(result).isNull();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithNullPageable_CacheHit_LogsHit() {
        Pageable pageable = null;
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        assertThat(result).isNotNull();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getPageFromCache_CacheMiss_LogsMiss() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        assertThat(result).isNull();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getPageFromCache_CacheHit_LogsHit() {
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        assertThat(result).isNotNull();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void invalidateCache_LogsClearedEntries() {
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        indexService.invalidateCache();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cache invalidated. Cleared 1 list entries and 1 page entries.") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putInCache_WithNullPageable_LogsCached() {
        Pageable pageable = null;
        listAppender.list.clear();

        indexService.putInCache(criteria, pageable, bookList);

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putPageInCache_LogsCached() {
        Pageable pageable = PageRequest.of(0, 10);
        listAppender.list.clear();

        indexService.putPageInCache(criteria, pageable, bookPage);

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached Page results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }
}