package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.AuthorDto;
import com.example.libraryapp.api.mapper.AuthorMapper;
import com.example.libraryapp.domain.Author;
import com.example.libraryapp.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Transactional
    public AuthorDto createAuthor(AuthorDto authorDto) {
        log.info("Creating author: {}", authorDto.getName());

        // Проверка на дубликат имени
        if (authorRepository.findByName(authorDto.getName()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Автор с именем " + authorDto.getName() + " уже существует");
        }

        Author author = authorMapper.toEntity(authorDto);

        try {
            Author savedAuthor = authorRepository.save(author);
            log.info("Author created successfully with id: {}", savedAuthor.getId());
            return authorMapper.toDto(savedAuthor);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating author: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Не удалось создать автора. Возможно, имя уже существует.");
        }
    }

    public List<AuthorDto> getAllAuthors() {
        log.debug("Getting all authors");
        return authorRepository.findAll()
                .stream()
                .map(authorMapper::toDto)
                .toList();
    }

    public AuthorDto getAuthorById(Long id) {
        log.debug("Getting author by id: {}", id);
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Author not found with id: " + id));
        return authorMapper.toDto(author);
    }

    @Transactional
    public AuthorDto updateAuthor(Long id, AuthorDto authorDto) {
        log.info("Updating author with id: {}", id);

        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Author not found with id: " + id));

        // Проверка на дубликат имени при обновлении (если имя изменилось)
        if (!author.getName().equals(authorDto.getName())
                && authorRepository.findByName(authorDto.getName()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Автор с именем " + authorDto.getName() + " уже существует");
        }

        author.setName(authorDto.getName());
        if (authorDto.getBiography() != null) {
            author.setBiography(authorDto.getBiography());
        }
        if (authorDto.getBirthDate() != null) {
            author.setBirthDate(authorDto.getBirthDate());
        }

        try {
            Author updatedAuthor = authorRepository.save(author);
            log.info("Author updated successfully");
            return authorMapper.toDto(updatedAuthor);
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while updating author: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Не удалось обновить автора. Возможно, имя уже используется.");
        }
    }

    @Transactional
    public void deleteAuthor(Long id) {
        log.info("Deleting author with id: {}", id);

        if (!authorRepository.existsById(id)) {
            throw new EntityNotFoundException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);
    }

    public List<AuthorDto> searchAuthorsByName(String name) {
        log.debug("Searching authors by name: {}", name);
        return authorRepository.searchByName(name)
                .stream()
                .map(authorMapper::toDto)
                .toList();
    }
}