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
import static org.junit.jupiter.api.Assertions.assertAll;

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

    @Test
    void getFromCache_WithNullPageable_ReturnsNullWhenCacheMiss() {
        List<BookResponseDto> result = indexService.getFromCache(criteria, null);

        assertThat(result).isNull();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithNullPageable_ReturnsValueWhenCacheHit() {
        indexService.putInCache(criteria, null, bookList);
        listAppender.list.clear();

        List<BookResponseDto> result = indexService.getFromCache(criteria, null);

        assertThat(result).isNotNull().isSameAs(bookList);
        assertThat(listAppender.list).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache HIT for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithUnpagedPageable_ReturnsValueWhenCacheHit() {
        Pageable unpaged = Pageable.unpaged();
        indexService.putInCache(criteria, unpaged, bookList);
        listAppender.list.clear();

        List<BookResponseDto> result = indexService.getFromCache(criteria, unpaged);

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result).isSameAs(bookList),
                () -> assertThat(listAppender.list).anyMatch(event ->
                        event.getFormattedMessage().contains("List Cache HIT for key:") &&
                                event.getLevel() == Level.INFO
                )
        );
    }

    @Test
    void getFromCache_WithPagedPageable_ReturnsNullWhenCacheMiss() {
        Pageable pageable = PageRequest.of(0, 10);
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        assertThat(result).isNull();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("List Cache MISS for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void getFromCache_WithPagedPageable_ReturnsValueWhenCacheHit() {
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        assertAll(
                () -> assertThat(result).isNotNull().isSameAs(bookList),
                () -> assertThat(listAppender.list).anyMatch(event ->
                        event.getFormattedMessage().contains("List Cache HIT for key:") &&
                                event.getLevel() == Level.INFO
                )
        );
    }

    @Test
    void getFromCache_WithPagedPageableAndSort_ReturnsValueWhenCacheHit() {
        Pageable pageable = PageRequest.of(1, 20, Sort.by("title").ascending());
        indexService.putInCache(criteria, pageable, bookList);
        listAppender.list.clear();

        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        assertAll(
                () -> assertThat(result).isNotNull(),
                () -> assertThat(result).isSameAs(bookList),
                () -> assertThat(listAppender.list).anyMatch(event ->
                        event.getFormattedMessage().contains("List Cache HIT for key:") &&
                                event.getLevel() == Level.INFO
                )
        );
    }

    @Test
    void putInCache_WithNullPageable_StoresAndLogs() {
        listAppender.list.clear();

        indexService.putInCache(criteria, null, bookList);

        List<BookResponseDto> cached = indexService.getFromCache(criteria, null);

        assertAll(
                () -> assertThat(cached).isSameAs(bookList),
                () -> assertThat(listAppender.list).anyMatch(event ->
                        event.getFormattedMessage().contains("Cached List results for key:") &&
                                event.getLevel() == Level.INFO
                )
        );
    }

    @Test
    void putInCache_WithUnpagedPageable_StoresAndLogs() {
        Pageable unpaged = Pageable.unpaged();
        listAppender.list.clear();

        indexService.putInCache(criteria, unpaged, bookList);

        List<BookResponseDto> cached = indexService.getFromCache(criteria, unpaged);
        assertThat(cached).isSameAs(bookList);

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putInCache_WithPagedPageable_StoresAndLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        listAppender.list.clear();

        indexService.putInCache(criteria, pageable, bookList);

        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        assertThat(cached).isSameAs(bookList);

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cached List results for key:") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void putInCache_OverwritesExistingEntry() {
        Pageable pageable = PageRequest.of(0, 10);
        List<BookResponseDto> firstList = List.of(bookDto);

        BookResponseDto secondBook = new BookResponseDto();
        secondBook.setId(2L);
        secondBook.setTitle("Анна Каренина");
        List<BookResponseDto> secondList = List.of(secondBook);

        indexService.putInCache(criteria, pageable, firstList);
        indexService.putInCache(criteria, pageable, secondList);

        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        assertThat(cached).isSameAs(secondList);
        assertThat(cached.get(0).getTitle()).isEqualTo("Анна Каренина");
    }

    @Test
    void getPageFromCache_ReturnsNullWhenCacheMiss() {
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
    void getPageFromCache_ReturnsValueWhenCacheHit() {
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        assertAll(
                () -> assertThat(result).isNotNull().isSameAs(bookPage),
                () -> assertThat(listAppender.list).anyMatch(event ->
                        event.getFormattedMessage().contains("Page Cache HIT for key:") &&
                                event.getLevel() == Level.INFO
                )
        );
    }

    @Test
    void putPageInCache_StoresAndLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        listAppender.list.clear();

        indexService.putPageInCache(criteria, pageable, bookPage);

        Page<BookResponseDto> cached = indexService.getPageFromCache(criteria, pageable);

        assertAll(
                () -> assertThat(cached).isSameAs(bookPage),
                () -> assertThat(listAppender.list).anyMatch(event ->
                        event.getFormattedMessage().contains("Cached Page results for key:") &&
                                event.getLevel() == Level.INFO
                )
        );
    }

    @Test
    void invalidateCache_ClearsAllEntriesAndLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        indexService.putPageInCache(criteria, pageable, bookPage);
        listAppender.list.clear();

        indexService.invalidateCache();

        assertThat(indexService.getCacheSize()).isZero();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cache invalidated. Cleared 1 list entries and 1 page entries.") &&
                        event.getLevel() == Level.INFO
        );
    }

    @Test
    void invalidateCache_OnEmptyCache_LogsZeroEntries() {
        listAppender.list.clear();

        indexService.invalidateCache();

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).anyMatch(event ->
                event.getFormattedMessage().contains("Cache invalidated. Cleared 0 list entries and 0 page entries.") &&
                        event.getLevel() == Level.INFO
        );
    }

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

    private Class<?> getCacheKeyClass() throws Exception {
        return Class.forName("com.example.libraryapp.service.IndexService$CacheKey");
    }
}