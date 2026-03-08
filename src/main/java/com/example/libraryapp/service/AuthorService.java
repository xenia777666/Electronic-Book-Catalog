package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.AuthorDto;
import com.example.libraryapp.api.mapper.AuthorMapper;
import com.example.libraryapp.domain.Author;
import com.example.libraryapp.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Transactional
    public AuthorDto createAuthor(AuthorDto authorDto) {
        log.info("Creating author: {}", authorDto.getName());
        Author author = authorMapper.toEntity(authorDto);
        Author savedAuthor = authorRepository.save(author);
        return authorMapper.toDto(savedAuthor);
    }

    public List<AuthorDto> getAllAuthors() {
        log.debug("Getting all authors");
        return authorRepository.findAll()
                .stream()
                .map(authorMapper::toDto)
                .collect(Collectors.toList());
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

        author.setName(authorDto.getName());
        if (authorDto.getBiography() != null) {
            author.setBiography(authorDto.getBiography());
        }
        if (authorDto.getBirthDate() != null) {
            author.setBirthDate(authorDto.getBirthDate());
        }

        Author updatedAuthor = authorRepository.save(author);
        return authorMapper.toDto(updatedAuthor);
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
                .collect(Collectors.toList());
    }
}