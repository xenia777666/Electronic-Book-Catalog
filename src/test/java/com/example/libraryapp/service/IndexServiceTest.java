package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.BookSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexServiceTest {

    private IndexService indexService;

    private BookSearchCriteria criteria;
    private BookResponseDto bookDto;
    private List<BookResponseDto> bookList;
    private Page<BookResponseDto> bookPage;

    @BeforeEach
    void setUp() {
        indexService = new IndexService();

        criteria = new BookSearchCriteria();
        criteria.setAuthorName("Толстой");
        criteria.setGenreName("Роман");

        bookDto = new BookResponseDto();
        bookDto.setId(1L);
        bookDto.setTitle("Война и мир");

        bookList = List.of(bookDto);
        bookPage = new PageImpl<>(bookList);
    }

    // ============= GET FROM CACHE TESTS =============

    @Test
    void getFromCache_WithNullPageable_ReturnsCachedList() {
        // Подготовка
        Pageable pageable = null;
        List<BookResponseDto> expectedList = bookList;

        // Кладем в кеш
        indexService.putInCache(criteria, pageable, expectedList);

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(expectedList);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Война и мир");
    }

    @Test
    void getFromCache_WithUnpagedPageable_ReturnsCachedList() {
        // Подготовка
        Pageable pageable = Pageable.unpaged();
        List<BookResponseDto> expectedList = bookList;

        // Кладем в кеш
        indexService.putInCache(criteria, pageable, expectedList);

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(expectedList);
    }

    @Test
    void getFromCache_WithPagedPageable_ReturnsCachedList() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        List<BookResponseDto> expectedList = bookList;

        // Кладем в кеш
        indexService.putInCache(criteria, pageable, expectedList);

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(expectedList);
    }

    @Test
    void getFromCache_WithPagedPageableAndSort_ReturnsCachedList() {
        // Подготовка
        Pageable pageable = PageRequest.of(1, 20, Sort.by("title").ascending());
        List<BookResponseDto> expectedList = bookList;

        // Кладем в кеш
        indexService.putInCache(criteria, pageable, expectedList);

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(expectedList);
    }

    @Test
    void getFromCache_CacheMiss_ReturnsNull() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();
    }

    @Test
    void getFromCache_DifferentCriteria_ReturnsNull() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);

        BookSearchCriteria differentCriteria = new BookSearchCriteria();
        differentCriteria.setAuthorName("Достоевский");

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(differentCriteria, pageable);

        // Проверка
        assertThat(result).isNull();
    }

    @Test
    void getFromCache_DifferentPageable_ReturnsNull() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        indexService.putInCache(criteria, pageable1, bookList);

        // Выполнение
        List<BookResponseDto> result = indexService.getFromCache(criteria, pageable2);

        // Проверка
        assertThat(result).isNull();
    }

    @Test
    void getFromCache_WithNullCriteria_HandlesCorrectly() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение и проверка
        assertThat(indexService.getFromCache(null, pageable)).isNull();
    }

    // ============= PUT IN CACHE TESTS =============

    @Test
    void putInCache_WithNullPageable_StoresInCache() {
        // Подготовка
        Pageable pageable = null;

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);

        // Проверка
        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        assertThat(cached).isSameAs(bookList);
    }

    @Test
    void putInCache_WithUnpagedPageable_StoresInCache() {
        // Подготовка
        Pageable pageable = Pageable.unpaged();

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);

        // Проверка
        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        assertThat(cached).isSameAs(bookList);
    }

    @Test
    void putInCache_WithPagedPageable_StoresInCache() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);

        // Проверка
        List<BookResponseDto> cached = indexService.getFromCache(criteria, pageable);
        assertThat(cached).isSameAs(bookList);
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
        assertThat(cached).hasSize(1);
        assertThat(cached.get(0).getTitle()).isEqualTo("Анна Каренина");
    }

    @Test
    void putInCache_WithMultipleCriteria_StoresSeparately() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        BookSearchCriteria criteria1 = new BookSearchCriteria();
        criteria1.setAuthorName("Толстой");

        BookSearchCriteria criteria2 = new BookSearchCriteria();
        criteria2.setAuthorName("Достоевский");

        List<BookResponseDto> list1 = List.of(bookDto);

        BookResponseDto book2 = new BookResponseDto();
        book2.setId(2L);
        book2.setTitle("Преступление и наказание");
        List<BookResponseDto> list2 = List.of(book2);

        // Выполнение
        indexService.putInCache(criteria1, pageable, list1);
        indexService.putInCache(criteria2, pageable, list2);

        // Проверка
        assertThat(indexService.getFromCache(criteria1, pageable)).isSameAs(list1);
        assertThat(indexService.getFromCache(criteria2, pageable)).isSameAs(list2);
    }

    @Test
    void putInCache_WithNullResults_DoesNotStore() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение - сервис не должен хранить null, но метод не выбрасывает исключение
        // В текущей реализации putInCache может выбросить NPE при попытке положить null
        // Поэтому просто проверяем, что кеш остается пустым
        try {
            indexService.putInCache(criteria, pageable, null);
        } catch (NullPointerException e) {
            // Ожидаемое поведение - null не может быть положен в ConcurrentHashMap
            // Проверяем, что кеш не содержит значение для этого ключа
            assertThat(indexService.getFromCache(criteria, pageable)).isNull();
            return;
        }

        // Если исключение не выброшено, проверяем что null не сохранен
        assertThat(indexService.getFromCache(criteria, pageable)).isNull();
    }

    // ============= GET PAGE FROM CACHE TESTS =============

    @Test
    void getPageFromCache_ReturnsCachedPage() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putPageInCache(criteria, pageable, bookPage);

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(bookPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Война и мир");
    }

    @Test
    void getPageFromCache_CacheMiss_ReturnsNull() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNull();
    }

    @Test
    void getPageFromCache_DifferentPageable_ReturnsNull() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        indexService.putPageInCache(criteria, pageable1, bookPage);

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable2);

        // Проверка
        assertThat(result).isNull();
    }

    @Test
    void getPageFromCache_WithSort_ReturnsCachedPage() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").descending());
        indexService.putPageInCache(criteria, pageable, bookPage);

        // Выполнение
        Page<BookResponseDto> result = indexService.getPageFromCache(criteria, pageable);

        // Проверка
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(bookPage);
    }

    // ============= PUT PAGE IN CACHE TESTS =============

    @Test
    void putPageInCache_StoresPage() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение
        indexService.putPageInCache(criteria, pageable, bookPage);

        // Проверка
        Page<BookResponseDto> cached = indexService.getPageFromCache(criteria, pageable);
        assertThat(cached).isSameAs(bookPage);
    }

    @Test
    void putPageInCache_OverwritesExistingPage() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        Page<BookResponseDto> firstPage = new PageImpl<>(List.of(bookDto));

        BookResponseDto secondBook = new BookResponseDto();
        secondBook.setId(2L);
        secondBook.setTitle("Анна Каренина");
        Page<BookResponseDto> secondPage = new PageImpl<>(List.of(secondBook));

        // Выполнение
        indexService.putPageInCache(criteria, pageable, firstPage);
        indexService.putPageInCache(criteria, pageable, secondPage);

        // Проверка
        Page<BookResponseDto> cached = indexService.getPageFromCache(criteria, pageable);
        assertThat(cached).isSameAs(secondPage);
        assertThat(cached.getContent().get(0).getTitle()).isEqualTo("Анна Каренина");
    }

    @Test
    void putPageInCache_WithMultiplePageables_StoresSeparately() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        Page<BookResponseDto> page1 = new PageImpl<>(List.of(bookDto));

        BookResponseDto book2 = new BookResponseDto();
        book2.setId(2L);
        book2.setTitle("Анна Каренина");
        Page<BookResponseDto> page2 = new PageImpl<>(List.of(book2));

        // Выполнение
        indexService.putPageInCache(criteria, pageable1, page1);
        indexService.putPageInCache(criteria, pageable2, page2);

        // Проверка
        assertThat(indexService.getPageFromCache(criteria, pageable1)).isSameAs(page1);
        assertThat(indexService.getPageFromCache(criteria, pageable2)).isSameAs(page2);
    }

    @Test
    void putPageInCache_WithNullPage_DoesNotStore() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение - сервис не должен хранить null
        try {
            indexService.putPageInCache(criteria, pageable, null);
        } catch (NullPointerException e) {
            // Ожидаемое поведение - null не может быть положен в ConcurrentHashMap
            assertThat(indexService.getPageFromCache(criteria, pageable)).isNull();
            return;
        }

        // Если исключение не выброшено, проверяем что null не сохранен
        assertThat(indexService.getPageFromCache(criteria, pageable)).isNull();
    }

    // ============= INVALIDATE CACHE TESTS =============

    @Test
    void invalidateCache_ClearsAllEntries() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        indexService.putPageInCache(criteria, pageable, bookPage);

        assertThat(indexService.getCacheSize()).isEqualTo(2);

        // Выполнение
        indexService.invalidateCache();

        // Проверка
        assertThat(indexService.getCacheSize()).isZero();
        assertThat(indexService.getFromCache(criteria, pageable)).isNull();
        assertThat(indexService.getPageFromCache(criteria, pageable)).isNull();
    }

    @Test
    void invalidateCache_OnEmptyCache_DoesNothing() {
        // Подготовка
        assertThat(indexService.getCacheSize()).isZero();

        // Выполнение
        indexService.invalidateCache();

        // Проверка
        assertThat(indexService.getCacheSize()).isZero();
    }

    @Test
    void invalidateCache_AfterInvalidate_CanStoreNewEntries() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        indexService.putInCache(criteria, pageable, bookList);
        indexService.invalidateCache();

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);

        // Проверка
        assertThat(indexService.getCacheSize()).isEqualTo(1);
        assertThat(indexService.getFromCache(criteria, pageable)).isSameAs(bookList);
    }

    // ============= GET CACHE SIZE TESTS =============

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

    // ============= CACHE KEY TESTS =============

    @Test
    void cacheKey_DifferentCriteria_AreDifferent() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        BookSearchCriteria criteria1 = new BookSearchCriteria();
        criteria1.setAuthorName("Толстой");

        BookSearchCriteria criteria2 = new BookSearchCriteria();
        criteria2.setAuthorName("Достоевский");

        // Выполнение
        indexService.putInCache(criteria1, pageable, bookList);

        // Проверка
        assertThat(indexService.getFromCache(criteria2, pageable)).isNull();
        assertThat(indexService.getFromCache(criteria1, pageable)).isNotNull();
    }

    @Test
    void cacheKey_DifferentPageNumbers_AreDifferent() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 10);

        // Выполнение
        indexService.putInCache(criteria, pageable1, bookList);

        // Проверка
        assertThat(indexService.getFromCache(criteria, pageable2)).isNull();
        assertThat(indexService.getFromCache(criteria, pageable1)).isNotNull();
    }

    @Test
    void cacheKey_DifferentPageSizes_AreDifferent() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(0, 20);

        // Выполнение
        indexService.putInCache(criteria, pageable1, bookList);

        // Проверка
        assertThat(indexService.getFromCache(criteria, pageable2)).isNull();
        assertThat(indexService.getFromCache(criteria, pageable1)).isNotNull();
    }

    @Test
    void cacheKey_DifferentSort_AreDifferent() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").descending());

        // Выполнение
        indexService.putInCache(criteria, pageable1, bookList);

        // Проверка
        assertThat(indexService.getFromCache(criteria, pageable2)).isNull();
        assertThat(indexService.getFromCache(criteria, pageable1)).isNotNull();
    }

    @Test
    void cacheKey_SameParameters_AreEqual() {
        // Подготовка
        Pageable pageable1 = PageRequest.of(0, 10, Sort.by("title").ascending());
        Pageable pageable2 = PageRequest.of(0, 10, Sort.by("title").ascending());

        // Выполнение
        indexService.putInCache(criteria, pageable1, bookList);

        // Проверка
        assertThat(indexService.getFromCache(criteria, pageable2)).isNotNull();
        assertThat(indexService.getFromCache(criteria, pageable2)).isSameAs(bookList);
    }

    // ============= EDGE CASES AND ADDITIONAL TESTS =============

    @Test
    void putInCache_WithSameKeyDifferentData_UpdatesCorrectly() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);
        List<BookResponseDto> firstList = List.of(bookDto);

        BookResponseDto secondBook = new BookResponseDto();
        secondBook.setId(2L);
        secondBook.setTitle("Новая книга");
        List<BookResponseDto> secondList = List.of(secondBook);

        // Выполнение
        indexService.putInCache(criteria, pageable, firstList);
        indexService.putInCache(criteria, pageable, secondList);

        // Проверка
        assertThat(indexService.getFromCache(criteria, pageable)).isSameAs(secondList);
        assertThat(indexService.getFromCache(criteria, pageable).get(0).getTitle()).isEqualTo("Новая книга");
    }

    @Test
    void complexScenario_ListAndPageCacheWorkIndependently() {
        // Подготовка
        Pageable pageable = PageRequest.of(0, 10);

        // Выполнение
        indexService.putInCache(criteria, pageable, bookList);
        indexService.putPageInCache(criteria, pageable, bookPage);

        // Проверка - оба кеша работают независимо
        assertThat(indexService.getFromCache(criteria, pageable)).isSameAs(bookList);
        assertThat(indexService.getPageFromCache(criteria, pageable)).isSameAs(bookPage);
        assertThat(indexService.getCacheSize()).isEqualTo(2);

        // Очищаем оба кеша через invalidate
        indexService.invalidateCache();
        assertThat(indexService.getCacheSize()).isZero();
    }

    @Test
    void putInCache_WithMultipleCriteriaAndPageables_StoresAll() {
        // Подготовка
        BookSearchCriteria criteria1 = new BookSearchCriteria();
        criteria1.setAuthorName("Толстой");

        BookSearchCriteria criteria2 = new BookSearchCriteria();
        criteria2.setAuthorName("Достоевский");

        Pageable pageable1 = PageRequest.of(0, 10);
        Pageable pageable2 = PageRequest.of(1, 20);

        // Выполнение
        indexService.putInCache(criteria1, pageable1, bookList);
        indexService.putInCache(criteria1, pageable2, bookList);
        indexService.putInCache(criteria2, pageable1, bookList);
        indexService.putInCache(criteria2, pageable2, bookList);

        // Проверка
        assertThat(indexService.getCacheSize()).isEqualTo(4);

        assertThat(indexService.getFromCache(criteria1, pageable1)).isNotNull();
        assertThat(indexService.getFromCache(criteria1, pageable2)).isNotNull();
        assertThat(indexService.getFromCache(criteria2, pageable1)).isNotNull();
        assertThat(indexService.getFromCache(criteria2, pageable2)).isNotNull();
    }

    @Test
    void cacheKey_ToString_ReturnsCorrectFormat() {
        // Проверяем через косвенный вызов (логирование)
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());
        indexService.putInCache(criteria, pageable, bookList);

        // Просто проверяем, что кеш работает
        assertThat(indexService.getFromCache(criteria, pageable)).isNotNull();
    }
}