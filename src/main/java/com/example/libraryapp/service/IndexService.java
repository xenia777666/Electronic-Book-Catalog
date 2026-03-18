package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.BookResponseDto;
import com.example.libraryapp.api.dto.BookSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class IndexService {

    // In-memory индекс на основе HashMap
    private final ConcurrentHashMap<String, List<BookResponseDto>> searchIndex = new ConcurrentHashMap<>();

    /**
     * Получить результаты из кэша
     */
    public List<BookResponseDto> getFromCache(BookSearchCriteria criteria, Pageable pageable) {
        String cacheKey = buildCacheKey(criteria, pageable);
        List<BookResponseDto> cached = searchIndex.get(cacheKey);

        if (cached != null) {
            log.info("Cache HIT for key: {}", cacheKey);
        } else {
            log.info("Cache MISS for key: {}", cacheKey);
        }

        return cached;
    }

    /**
     * Сохранить результаты в кэш
     */
    public void putInCache(BookSearchCriteria criteria, Pageable pageable, List<BookResponseDto> results) {
        String cacheKey = buildCacheKey(criteria, pageable);
        searchIndex.put(cacheKey, results);
        log.info("Cached results for key: {}", cacheKey);
    }

    /**
     * Инвалидация всего кэша при изменении данных
     */
    public void invalidateCache() {
        int size = searchIndex.size();
        searchIndex.clear();
        log.info("Cache invalidated. Cleared {} entries.", size);
    }

    /**
     * Построение составного ключа из параметров запроса
     */
    private String buildCacheKey(BookSearchCriteria criteria, Pageable pageable) {
        return String.format("%s|%d|%d",
                criteria.getCacheKey(),
                pageable.getPageNumber(),
                pageable.getPageSize()
        );
    }

    /**
     * Получить размер кэша (для мониторинга)
     */
    public int getCacheSize() {
        return searchIndex.size();
    }
}