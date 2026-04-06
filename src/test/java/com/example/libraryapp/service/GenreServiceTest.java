package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.GenreDto;
import com.example.libraryapp.api.mapper.GenreMapper;
import com.example.libraryapp.domain.Genre;
import com.example.libraryapp.repository.GenreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private GenreMapper genreMapper;

    private GenreService genreService;
    private Genre genre;
    private GenreDto responseDto;

    @BeforeEach
    void setUp() {
        genreService = new GenreService(genreRepository, genreMapper);

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Роман");
        genre.setDescription("Литературный жанр, характеризующийся развернутым повествованием");

        responseDto = new GenreDto();
        responseDto.setId(1L);
        responseDto.setName("Роман");
        responseDto.setDescription("Литературный жанр, характеризующийся развернутым повествованием");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("updateGenreTestData")
    void updateGenre_ParameterizedTest(String testName, GenreDto updateDto,
                                       String expectedName, String expectedDescription,
                                       boolean shouldSetName, boolean shouldSetDescription) {
        // Given
        Genre genreSpy = spy(genre);
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        // When
        genreService.updateGenre(1L, updateDto);

        // Then
        if (shouldSetName) {
            verify(genreSpy).setName(expectedName);
        } else {
            verify(genreSpy, never()).setName(any());
        }

        if (shouldSetDescription) {
            verify(genreSpy).setDescription(expectedDescription);
        } else {
            verify(genreSpy, never()).setDescription(any());
        }
    }

    private static Stream<Arguments> updateGenreTestData() {
        return Stream.of(
                Arguments.of(
                        "Only name updated when description is null",
                        createUpdateDto("Новый роман", null),
                        "Новый роман", null,
                        true, false
                ),
                Arguments.of(
                        "Only description updated when name same",
                        createUpdateDto("Роман", "Обновленное описание"),
                        "Роман", "Обновленное описание",
                        true, true
                ),
                Arguments.of(
                        "With null description does not update description",
                        createUpdateDto("Обновленный роман", null),
                        "Обновленный роман", null,
                        true, false
                ),
                Arguments.of(
                        "With null name updates name to null",
                        createUpdateDto(null, "Новое описание"),
                        null, "Новое описание",
                        true, true
                ),
                Arguments.of(
                        "With empty name updates to empty",
                        createUpdateDto("", "Описание"),
                        "", "Описание",
                        true, true
                ),
                Arguments.of(
                        "With empty description updates to empty",
                        createUpdateDto("Роман", ""),
                        "Роман", "",
                        true, true
                ),
                Arguments.of(
                        "With both fields null updates name to null, description unchanged",
                        createUpdateDto(null, null),
                        null, null,
                        true, false
                ),
                Arguments.of(
                        "With both fields empty updates both to empty",
                        createUpdateDto("", ""),
                        "", "",
                        true, true
                )
        );
    }

    private static GenreDto createUpdateDto(String name, String description) {
        GenreDto dto = new GenreDto();
        dto.setName(name);
        dto.setDescription(description);
        return dto;
    }

    // ============= ОСТАЛЬНЫЕ ТЕСТЫ =============

    @org.junit.jupiter.api.Test
    void updateGenre_NotFound_ThrowsEntityNotFound() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("Новый жанр");

        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.updateGenre(999L, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genre not found with id: 999");

        verify(genreRepository).findById(999L);
        verify(genreRepository, never()).save(any());
    }

    @org.junit.jupiter.api.Test
    void createGenre_Success() {
        GenreDto genreDto = new GenreDto();
        genreDto.setName("Роман");
        genreDto.setDescription("Описание");

        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Роман");

        when(genreMapper.toEntity(genreDto)).thenReturn(genre);
        when(genreRepository.save(genre)).thenReturn(genre);
        when(genreMapper.toDto(genre)).thenReturn(responseDto);

        GenreDto result = genreService.createGenre(genreDto);

        org.assertj.core.api.Assertions.assertThat(result).isNotNull();
        verify(genreMapper).toEntity(genreDto);
        verify(genreRepository).save(genre);
    }

    @org.junit.jupiter.api.Test
    void getAllGenres_Success() {
        when(genreRepository.findAll()).thenReturn(java.util.List.of(genre));
        when(genreMapper.toDto(genre)).thenReturn(responseDto);

        java.util.List<GenreDto> result = genreService.getAllGenres();

        org.assertj.core.api.Assertions.assertThat(result).hasSize(1);
        verify(genreRepository).findAll();
    }

    @org.junit.jupiter.api.Test
    void getGenreById_Success() {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
        when(genreMapper.toDto(genre)).thenReturn(responseDto);

        GenreDto result = genreService.getGenreById(1L);

        org.assertj.core.api.Assertions.assertThat(result).isNotNull();
        verify(genreRepository).findById(1L);
    }

    @org.junit.jupiter.api.Test
    void getGenreById_NotFound_ThrowsEntityNotFound() {
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.getGenreById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @org.junit.jupiter.api.Test
    void deleteGenre_Success() {
        when(genreRepository.existsById(1L)).thenReturn(true);
        doNothing().when(genreRepository).deleteById(1L);

        genreService.deleteGenre(1L);

        verify(genreRepository).existsById(1L);
        verify(genreRepository).deleteById(1L);
    }

    @org.junit.jupiter.api.Test
    void deleteGenre_NotFound_ThrowsEntityNotFound() {
        when(genreRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> genreService.deleteGenre(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}