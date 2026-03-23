package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.BookSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class IndexService {

    private final ConcurrentHashMap<CacheKey, List<BookResponseDto>> searchIndex = new ConcurrentHashMap<>();

    public List<BookResponseDto> getFromCache(BookSearchCriteria criteria, Pageable pageable) {
        // Проверяем, что pageable не null и не unpaged
        if (pageable == null || pageable.isUnpaged()) {
            // Для запросов без пагинации используем специальный ключ с pageNumber = -1
            CacheKey key = new CacheKey(criteria, -1, -1, null);
            List<BookResponseDto> cached = searchIndex.get(key);
            if (cached != null) {
                log.info("Cache HIT for key: {}", key);
            } else {
                log.info("Cache MISS for key: {}", key);
            }
            return cached;
        }

        CacheKey key = new CacheKey(criteria, pageable);
        List<BookResponseDto> cached = searchIndex.get(key);

        if (cached != null) {
            log.info("Cache HIT for key: {}", key);
        } else {
            log.info("Cache MISS for key: {}", key);
        }

        return cached;
    }

    public void putInCache(BookSearchCriteria criteria, Pageable pageable, List<BookResponseDto> results) {
        if (pageable == null || pageable.isUnpaged()) {
            CacheKey key = new CacheKey(criteria, -1, -1, null);
            searchIndex.put(key, results);
            log.info("Cached results for key: {}", key);
            return;
        }

        CacheKey key = new CacheKey(criteria, pageable);
        searchIndex.put(key, results);
        log.info("Cached results for key: {}", key);
    }

    public void invalidateCache() {
        int size = searchIndex.size();
        searchIndex.clear();
        log.info("Cache invalidated. Cleared {} entries.", size);
    }

    public int getCacheSize() {
        return searchIndex.size();
    }

    // ============= ВНУТРЕННИЙ КЛАСС-КЛЮЧ =============
    private static class CacheKey {
        private final BookSearchCriteria criteria;
        private final int pageNumber;
        private final int pageSize;
        private final String sort;

        public CacheKey(BookSearchCriteria criteria, Pageable pageable) {
            this.criteria = criteria;
            this.pageNumber = pageable.getPageNumber();
            this.pageSize = pageable.getPageSize();
            this.sort = pageable.getSort().toString();
        }

        // Конструктор для запросов без пагинации
        public CacheKey(BookSearchCriteria criteria, int pageNumber, int pageSize, String sort) {
            this.criteria = criteria;
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.sort = sort;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CacheKey cacheKey = (CacheKey) o;
            return pageNumber == cacheKey.pageNumber
                    && pageSize == cacheKey.pageSize
                    && Objects.equals(criteria, cacheKey.criteria)
                    && Objects.equals(sort, cacheKey.sort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(criteria, pageNumber, pageSize, sort);
        }

        @Override
        public String toString() {
            return String.format("%s|%d|%d|%s",
                    criteria.getCacheKey(),
                    pageNumber,
                    pageSize,
                    sort != null ? sort : "unsorted"
            );
        }
    }
}