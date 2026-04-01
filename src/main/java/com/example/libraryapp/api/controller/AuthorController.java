package com.example.libraryapp.api.controller;

import com.example.libraryapp.api.dto.AuthorDto;
import com.example.libraryapp.service.AuthorService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    @Operation(summary = "Создать нового автора")
    @ApiResponse(responseCode = "201", description = "Автор создан")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @PostMapping
    public ResponseEntity<AuthorDto> createAuthor(@Valid @RequestBody AuthorDto authorDto) {
        log.info("POST /api/authors - Creating author: {}", authorDto.getName());
        AuthorDto created = authorService.createAuthor(authorDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Получить всех авторов")
    @ApiResponse(responseCode = "200", description = "Авторы найдены")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @GetMapping
    public ResponseEntity<List<AuthorDto>> getAllAuthors() {
        log.info("GET /api/authors");
        List<AuthorDto> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    @Operation(summary = "Найти автора по айди")
    @ApiResponse(responseCode = "200", description = "Автор найден")
    @GetMapping("/{id}")
    public ResponseEntity<AuthorDto> getAuthorById(@PathVariable Long id) {
        log.info("GET /api/authors/{}", id);
        AuthorDto author = authorService.getAuthorById(id);
        return ResponseEntity.ok(author);
    }

    @Operation(summary = "Обновить информацию об авторе по айди")
    @PutMapping("/{id}")
    public ResponseEntity<AuthorDto> updateAuthor(
            @PathVariable Long id,
            @Valid @RequestBody AuthorDto authorDto) {
        log.info("PUT /api/authors/{} - Updating author", id);
        AuthorDto updated = authorService.updateAuthor(id, authorDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить автора по айди")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        log.info("DELETE /api/authors/{}", id);
        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Найти автора по имени")
    @ApiResponse(responseCode = "200", description = "Автор найден")
    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
    @GetMapping("/search")
    public ResponseEntity<List<AuthorDto>> searchAuthorsByName(
            @RequestParam String name) {
        log.info("GET /api/authors/search?name={}", name);
        List<AuthorDto> authors = authorService.searchAuthorsByName(name);
        return ResponseEntity.ok(authors);
    }
}