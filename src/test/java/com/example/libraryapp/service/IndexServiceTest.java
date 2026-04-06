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

    // ============= GET FROM CACHE TESTS - COVERING LINE 25 AND 37-50 =============

    @Test
    void getFromCache_WithNullPageable_ReturnsNullWhenCacheMiss() {
        // Выполнение - pageable = null, кеш пуст
        List<BookResponseDto> result = indexService.getFromCache(criteria, null);

        // Проверка
        assertThat(result).isNull();

        // Проверка лога MISS
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithNullPageable_ReturnsValueWhenCacheHit() {
        // Подготовка - кладем в кеш с pageable = null
        indexService.putInCache(criteria, null, bookList);
        listAppender.list.clear();

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, null);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(bookList);

        // Проверка лога HIT
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithUnpagedPageable_ReturnsNullWhenCacheMiss() {
        // Выполнение - pageable.unpaged(), кеш пуст
        Pageable unpaged = Pageable.unpaged();
        List<BookResponseDto> result = indexService.getFromCache(criteria, unpaged);

        // Проверка
        assertThat(result).isNull();

        // Проверка лога MISS
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithUnpagedPageable_ReturnsValueWhenCacheHit() {
        // Подготовка - кладем в кеш с unpaged
        Pageable unpaged = Pageable.unpaged();
        indexService.putInCache(criteria, unpaged, bookList);
        listAppender.list.clear();

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, unpaged);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(bookList);

        // Проверка лога HIT
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithPagedPageable_ReturnsNullWhenCacheMiss() {
        // Выполнение - pageable с пагинацией, кеш пуст
        Pageable pageable = PageRequest.of(0, 10);
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();

        // Проверка лога MISS
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithPagedPageable_ReturnsValueWhenCacheHit() {
        // Подготовка - кладем в кеш с пагинацией
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(bookList);

        // Проверка лога HIT
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithPagedPageableAndSort_ReturnsValueWhenCacheHit() {
        // Подготовка - кладем в кеш с сортировкой
        Pageable pageable = PageRequest.of(1, 20, Sort.by("title").ascending());
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(bookList);

        // Проверка лога HIT
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= PUT IN CACHE TESTS - COVERING LINES WITH PARTIAL COVERAGE =============

    @Test
    void putInCache_WithNullPageable_StoresAndLogs() {
        // Подготовка
        listAppender.list.clear();

        // Выполнение
        indexService.putInCache(criteria, null, bookList);

        // Проверка - значение сохранено
        List<BookResponseDto> cached = indexService.getFromCache(criteria, null);
        assertThat(cached).isSameAs(bookList);

        // Проверка лога
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putInCache_WithUnpagedPageable_StoresAndLogs() {
        // Подготовка
        Pageable unpaged = Pageable.unpaged();
        listAppender.list.clear();

        // Выполнение
        indexService.putInCache(criteria, unpaged, bookList);

        // Проверка - значение сохранено
        List<BookResponseDto> cached = indexService.getFromCache(criteria, unpaged);
        assertThat(cached).isSameAs(bookList);

        // Проверка лога
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putInCache_WithPagedPageable_StoresAndLogs() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        listAppender.list.clear();

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);

        // Проверка - значение сохранено
        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        assertThat(cached).isSameAs(bookList);

        // Проверка лога
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putInCache_OverwritesExistingEntry() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        List<BookResponseDto> firstList = List.of(bookDto);

        BookResponseDto secondBook = new BookResponseDto();
        secondBook.setId(2L);
        secondBook.setTitle("Анна Каренина");
        List<BookResponseDto> secondList = List.of(secondBook);

        // Выполнение
        indexService.putInCache(criteria, pageable, firstList);
        indexService.putInCache(criteria, pageable, secondList);

        // Проверка
        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        assertThat(cached).isSameAs(secondList);
        assertThat(cached.get(0).getTitle()).isEqualTo("Анна Каренина");
    }

    // ============= GET PAGE FROM CACHE TESTS =============

    @Test
    void getPageFromCache_ReturnsNullWhenCacheMiss() {
        // Выполнение
        Pageable pageable = PageRequest.of(0, 10);
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();

        // Проверка лога MISS
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getPageFromCache_ReturnsValueWhenCacheHit() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(bookPage);

        // Проверка лога HIT
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Page Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= PUT PAGE IN CACHE TESTS =============

    @Test
    void putPageInCache_StoresAndLogs() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        listAppender.list.clear();

        // Выполнение
        indexService.putPageInCache(criteria, pageable, bookPage);

        // Проверка
        Page<BookResponseDto> cached = indexService.getPageFromCache(criteria, pageable);
        assertThat(cached).isSameAs(bookPage);

        // Проверка лога
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached Page results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= INVALIDATE CACHE TESTS =============

    @Test
    void invalidateCache_ClearsAllEntriesAndLogs() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        // Выполнение
        indexService.invalidateCache();

        // Проверка
        assertThat(indexService.getCacheSize()).isZero();

        // Проверка лога
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

        // Проверка лога
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cache invalidated. Cleared 0 list entries and 0 page entries.") &&
                        event.getLevel() == Level.INFO
        );
    }

    // ============= GET CACHE SIZE TESTS =============

    @Test
    void getCacheSize_EmptyCache_ReturnsZero() {
        assertThat(indexService.getCacheSize()).isZero();
    }

    @Test
    void getCacheSize_WithEntries_ReturnsCorrectSize() {
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        indexService.putInCache(criteria, pageable1, bookList);
        indexService.putPageInCache(criteria, pageable2, bookPage);

        assertThat(indexService.getCacheSize()).isEqualTo(2);
    }

    // ============= TESTS FOR CacheKey EQUALS METHOD USING REFLECTION =============

    @Test
    void cacheKey_Equals_SameObject_ReturnsTrue() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable = PageRequest.of(0, 10);
        Object key1 = constructor.newInstance(criteria, pageable);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key1);
        assertThat(result).isTrue();
    }

    @Test
    void cacheKey_Equals_NullObject_ReturnsFalse() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable = PageRequest.of(0, 10);
        Object key1 = constructor.newInstance(criteria, pageable);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, new Object[]{null});
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_DifferentClass_ReturnsFalse() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable = PageRequest.of(0, 10);
        Object key1 = constructor.newInstance(criteria, pageable);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, "test");
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_SamePageNumberAndPageSize_ReturnsTrue() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(0, 10);

        Object key1 = constructor.newInstance(criteria, pageable1);
        Object key2 = constructor.newInstance(criteria, pageable2);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isTrue();
    }

    @Test
    void cacheKey_Equals_DifferentPageNumber_ReturnsFalse() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        Object key1 = constructor.newInstance(criteria, pageable1);
        Object key2 = constructor.newInstance(criteria, pageable2);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_DifferentPageSize_ReturnsFalse() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(0, 20);

        Object key1 = constructor.newInstance(criteria, pageable1);
        Object key2 = constructor.newInstance(criteria, pageable2);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_DifferentCriteria_ReturnsFalse() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        BookSearchCriteria criteria2 = new BookSearchCriteria();
        criteria2.setAuthorName("Достоевский");

        Pageable pageable = PageRequest.of(0, 10);

        Object key1 = constructor.newInstance(criteria, pageable);
        Object key2 = constructor.newInstance(criteria2, pageable);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_DifferentSort_ReturnsFalse() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").descending());

        Object key1 = constructor.newInstance(criteria, pageable1);
        Object key2 = constructor.newInstance(criteria, pageable2);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isFalse();
    }

    @Test
    void cacheKey_Equals_WithNullSort_ReturnsTrue() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, int.class, int.class, String.class);
        constructor.setAccessible(true);

        Object key1 = constructor.newInstance(criteria, 0, 10, null);
        Object key2 = constructor.newInstance(criteria, 0, 10, null);

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isTrue();
    }

    @Test
    void cacheKey_Equals_WithNullCriteria_ReturnsTrue() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, int.class, int.class, String.class);
        constructor.setAccessible(true);

        Object key1 = constructor.newInstance(null, 0, 10, "title: ASC");
        Object key2 = constructor.newInstance(null, 0, 10, "title: ASC");

        Method equalsMethod = cacheKeyClass.getDeclaredMethod("equals", Object.class);
        equalsMethod.setAccessible(true);

        boolean result = (boolean) equalsMethod.invoke(key1, key2);
        assertThat(result).isTrue();
    }

    // ============= TESTS FOR CacheKey HASHCODE METHOD =============

    @Test
    void cacheKey_HashCode_EqualObjects_ReturnsSameHashCode() throws Exception {
        Class<?> cacheKeyClass = getCacheKeyClass();
        Constructor<?> constructor = cacheKeyClass.getDeclaredConstructor(BookSearchCriteria.class, Pageable.class);
        constructor.setAccessible(true);

        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").ascending());

        Object key1 = constructor.newInstance(criteria, pageable1);
        Object key2 = constructor.newInstance(criteria, pageable2);

        Method hashCodeMethod = cacheKeyClass.getDeclaredMethod("hashCode");
        hashCodeMethod.setAccessible(true);

        int hashCode1 = (int) hashCodeMethod.invoke(key1);
        int hashCode2 = (int) hashCodeMethod.invoke(key2);

        assertThat(hashCode1).isEqualTo(hashCode2);
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
}