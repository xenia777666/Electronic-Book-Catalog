package com.example.libraryapp.api.controller;

import com.example.libraryapp.api.dto.GenreDto;
import com.example.libraryapp.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @Operation(summary = "Создать новый жанр")
    @PostMapping
    public ResponseEntity<GenreDto> createGenre(@Valid @RequestBody GenreDto genreDto) {
        log.info("POST /api/genres - Creating genre: {}", genreDto.getName());
        GenreDto created = genreService.createGenre(genreDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Посмотреть все жанры")
    @ApiResponse(responseCode = "200", description = "Жанры найдены")
    @GetMapping
    public ResponseEntity<List<GenreDto>> getAllGenres() {
        log.info("GET /api/genres");
        List<GenreDto> genres = genreService.getAllGenres();
        return ResponseEntity.ok(genres);
    }

    @Operation(summary = "Посмотреть жанр по айди")
    @ApiResponse(responseCode = "200", description = "Жанр найден")
    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> getGenreById(@PathVariable Long id) {
        log.info("GET /api/genres/{}", id);
        GenreDto genre = genreService.getGenreById(id);
        return ResponseEntity.ok(genre);
    }

    @Operation(summary = "Изменить жанр по айди")
    @PutMapping("/{id}")
    public ResponseEntity<GenreDto> updateGenre(
            @PathVariable Long id,
            @Valid @RequestBody GenreDto genreDto) {
        log.info("PUT /api/genres/{} - Updating genre", id);
        GenreDto updated = genreService.updateGenre(id, genreDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить жанр по айди")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
        log.info("DELETE /api/genres/{}", id);
        genreService.deleteGenre(id);
        return ResponseEntity.noContent().build();
    }
}