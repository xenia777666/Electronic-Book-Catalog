package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.BookSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class IndexService {

    private final ConcurrentHashMap<CacheKey, List<BookResponseDto>> searchIndex = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<CacheKey, Page<BookResponseDto>> pageIndex = new ConcurrentHashMap<>();


    public List<BookResponseDto> getFromCache(BookSearchCriteria criteria, Pageable pageable) {

        if (pageable == null || pageable.isUnpaged()) {

            CacheKey key = new CacheKey(criteria, -1, -1, null);
            List<BookResponseDto> cached = searchIndex.get(key);
            if (cached != null) {
                log.info("List Cache HIT for key: {}", key);
            } else {
                log.info("List Cache MISS for key: {}", key);
            }
            return cached;
        }

        CacheKey key = new CacheKey(criteria, pageable);
        List<BookResponseDto> cached = searchIndex.get(key);

        if (cached != null) {
            log.info("List Cache HIT for key: {}", key);
        } else {
            log.info("List Cache MISS for key: {}", key);
        }

        return cached;
    }

    public void putInCache(BookSearchCriteria criteria, Pageable pageable, List<BookResponseDto> results) {
        if (pageable == null || pageable.isUnpaged()) {
            CacheKey key = new CacheKey(criteria, -1, -1, null);
            searchIndex.put(key, results);
            log.info("Cached List results for key: {}", key);
            return;
        }

        CacheKey key = new CacheKey(criteria, pageable);
        searchIndex.put(key, results);
        log.info("Cached List results for key: {}", key);
    }


    public Page<BookResponseDto> getPageFromCache(BookSearchCriteria criteria, Pageable pageable) {
        CacheKey key = new CacheKey(criteria, pageable);
        Page<BookResponseDto> cached = pageIndex.get(key);

        if (cached != null) {
            log.info("Page Cache HIT for key: {}", key);
        } else {
            log.info("Page Cache MISS for key: {}", key);
        }

        return cached;
    }

    public void putPageInCache(BookSearchCriteria criteria, Pageable pageable, Page<BookResponseDto> page) {
        CacheKey key = new CacheKey(criteria, pageable);
        pageIndex.put(key, page);
        log.info("Cached Page results for key: {}", key);
    }


    public void invalidateCache() {
        int listSize = searchIndex.size();
        int pageSize = pageIndex.size();
        searchIndex.clear();
        pageIndex.clear();
        log.info("Cache invalidated. Cleared {} list entries and {} page entries.", listSize, pageSize);
    }

    public int getCacheSize() {
        return searchIndex.size() + pageIndex.size();
    }


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