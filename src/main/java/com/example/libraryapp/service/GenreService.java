package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.GenreDto;
import com.example.libraryapp.api.mapper.GenreMapper;
import com.example.libraryapp.domain.Genre;
import com.example.libraryapp.repository.GenreRepository;
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
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    @Transactional
    public GenreDto createGenre(GenreDto genreDto) {
        log.info("Creating genre: {}", genreDto.getName());
        Genre genre = genreMapper.toEntity(genreDto);
        Genre savedGenre = genreRepository.save(genre);
        return genreMapper.toDto(savedGenre);
    }

    public List<GenreDto> getAllGenres() {
        log.debug("Getting all genres");
        return genreRepository.findAll()
                .stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toList());
    }

    public GenreDto getGenreById(Long id) {
        log.debug("Getting genre by id: {}", id);
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Genre not found with id: " + id));
        return genreMapper.toDto(genre);
    }

    @Transactional
    public GenreDto updateGenre(Long id, GenreDto genreDto) {
        log.info("Updating genre with id: {}", id);

        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Genre not found with id: " + id));

        genre.setName(genreDto.getName());
        if (genreDto.getDescription() != null) {
            genre.setDescription(genreDto.getDescription());
        }

        Genre updatedGenre = genreRepository.save(genre);
        return genreMapper.toDto(updatedGenre);
    }

    @Transactional
    public void deleteGenre(Long id) {
        log.info("Deleting genre with id: {}", id);

        if (!genreRepository.existsById(id)) {
            throw new EntityNotFoundException("Genre not found with id: " + id);
        }
        genreRepository.deleteById(id);
    }
}