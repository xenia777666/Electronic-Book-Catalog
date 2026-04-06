package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.GenreDto;
import com.example.libraryapp.api.mapper.GenreMapper;
import com.example.libraryapp.domain.Genre;
import com.example.libraryapp.repository.GenreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    private GenreDto genreDto;
    private Genre genre;
    private GenreDto responseDto;

    @BeforeEach
    void setUp() {
        genreService = new GenreService(genreRepository, genreMapper);

        genreDto = new GenreDto();
        genreDto.setName("Роман");
        genreDto.setDescription("Литературный жанр, характеризующийся развернутым повествованием");

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Роман");
        genre.setDescription("Литературный жанр, характеризующийся развернутым повествованием");

        responseDto = new GenreDto();
        responseDto.setId(1L);
        responseDto.setName("Роман");
        responseDto.setDescription("Литературный жанр, характеризующийся развернутым повествованием");
    }

    // ============= CREATE GENRE TESTS =============

    @Test
    void createGenre_Success() {
        when(genreMapper.toEntity(genreDto)).thenReturn(genre);
        when(genreRepository.save(genre)).thenReturn(genre);
        when(genreMapper.toDto(genre)).thenReturn(responseDto);

        GenreDto result = genreService.createGenre(genreDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Роман");
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Литературный жанр, характеризующийся развернутым повествованием");

        verify(genreMapper).toEntity(genreDto);
        verify(genreRepository).save(genre);
        verify(genreMapper).toDto(genre);
    }

    @Test
    void createGenre_WithMinimalData_Success() {
        GenreDto minimalDto = new GenreDto();
        minimalDto.setName("Поэзия");

        Genre minimalGenre = new Genre();
        minimalGenre.setId(2L);
        minimalGenre.setName("Поэзия");

        GenreDto minimalResponse = new GenreDto();
        minimalResponse.setId(2L);
        minimalResponse.setName("Поэзия");

        when(genreMapper.toEntity(minimalDto)).thenReturn(minimalGenre);
        when(genreRepository.save(minimalGenre)).thenReturn(minimalGenre);
        when(genreMapper.toDto(minimalGenre)).thenReturn(minimalResponse);

        GenreDto result = genreService.createGenre(minimalDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Поэзия");
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getDescription()).isNull();

        verify(genreMapper).toEntity(minimalDto);
        verify(genreRepository).save(minimalGenre);
        verify(genreMapper).toDto(minimalGenre);
    }

    @Test
    void createGenre_WithNullDescription_Success() {
        GenreDto dtoWithNullDesc = new GenreDto();
        dtoWithNullDesc.setName("Драма");
        dtoWithNullDesc.setDescription(null);

        Genre genreWithNullDesc = new Genre();
        genreWithNullDesc.setId(3L);
        genreWithNullDesc.setName("Драма");
        genreWithNullDesc.setDescription(null);

        GenreDto responseWithNullDesc = new GenreDto();
        responseWithNullDesc.setId(3L);
        responseWithNullDesc.setName("Драма");

        when(genreMapper.toEntity(dtoWithNullDesc)).thenReturn(genreWithNullDesc);
        when(genreRepository.save(genreWithNullDesc)).thenReturn(genreWithNullDesc);
        when(genreMapper.toDto(genreWithNullDesc)).thenReturn(responseWithNullDesc);

        GenreDto result = genreService.createGenre(dtoWithNullDesc);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Драма");
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void createGenre_WithEmptyDescription_Success() {
        GenreDto dtoWithEmptyDesc = new GenreDto();
        dtoWithEmptyDesc.setName("Трагедия");
        dtoWithEmptyDesc.setDescription("");

        Genre genreWithEmptyDesc = new Genre();
        genreWithEmptyDesc.setId(4L);
        genreWithEmptyDesc.setName("Трагедия");
        genreWithEmptyDesc.setDescription("");

        GenreDto responseWithEmptyDesc = new GenreDto();
        responseWithEmptyDesc.setId(4L);
        responseWithEmptyDesc.setName("Трагедия");
        responseWithEmptyDesc.setDescription("");

        when(genreMapper.toEntity(dtoWithEmptyDesc)).thenReturn(genreWithEmptyDesc);
        when(genreRepository.save(genreWithEmptyDesc)).thenReturn(genreWithEmptyDesc);
        when(genreMapper.toDto(genreWithEmptyDesc)).thenReturn(responseWithEmptyDesc);

        GenreDto result = genreService.createGenre(dtoWithEmptyDesc);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Трагедия");
        assertThat(result.getDescription()).isEmpty();
    }

    @Test
    void createGenre_WithVeryLongName_ShouldHandle() {
        String veryLongName = "A".repeat(100);
        GenreDto longNameDto = new GenreDto();
        longNameDto.setName(veryLongName);

        Genre genreWithLongName = new Genre();
        genreWithLongName.setId(5L);
        genreWithLongName.setName(veryLongName);

        GenreDto responseWithLongName = new GenreDto();
        responseWithLongName.setId(5L);
        responseWithLongName.setName(veryLongName);

        when(genreMapper.toEntity(longNameDto)).thenReturn(genreWithLongName);
        when(genreRepository.save(genreWithLongName)).thenReturn(genreWithLongName);
        when(genreMapper.toDto(genreWithLongName)).thenReturn(responseWithLongName);

        GenreDto result = genreService.createGenre(longNameDto);

        assertThat(result.getName()).isEqualTo(veryLongName);
    }

    // ============= GET ALL GENRES TESTS =============

    @Test
    void getAllGenres_Success() {
        when(genreRepository.findAll()).thenReturn(List.of(genre));
        when(genreMapper.toDto(genre)).thenReturn(responseDto);

        List<GenreDto> result = genreService.getAllGenres();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Роман");
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(genreRepository).findAll();
        verify(genreMapper).toDto(genre);
    }

    @Test
    void getAllGenres_EmptyList_ReturnsEmptyList() {
        when(genreRepository.findAll()).thenReturn(Collections.emptyList());

        List<GenreDto> result = genreService.getAllGenres();

        assertThat(result).isEmpty();
        verify(genreRepository).findAll();
        verify(genreMapper, never()).toDto(any());
    }

    @Test
    void getAllGenres_MultipleGenres_ReturnsAll() {
        Genre genre2 = new Genre();
        genre2.setId(2L);
        genre2.setName("Поэзия");
        genre2.setDescription("Стихотворная форма");

        GenreDto responseDto2 = new GenreDto();
        responseDto2.setId(2L);
        responseDto2.setName("Поэзия");
        responseDto2.setDescription("Стихотворная форма");

        Genre genre3 = new Genre();
        genre3.setId(3L);
        genre3.setName("Драма");
        genre3.setDescription("Сценическое искусство");

        GenreDto responseDto3 = new GenreDto();
        responseDto3.setId(3L);
        responseDto3.setName("Драма");
        responseDto3.setDescription("Сценическое искусство");

        when(genreRepository.findAll()).thenReturn(List.of(genre, genre2, genre3));
        when(genreMapper.toDto(genre)).thenReturn(responseDto);
        when(genreMapper.toDto(genre2)).thenReturn(responseDto2);
        when(genreMapper.toDto(genre3)).thenReturn(responseDto3);

        List<GenreDto> result = genreService.getAllGenres();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("Роман");
        assertThat(result.get(1).getName()).isEqualTo("Поэзия");
        assertThat(result.get(2).getName()).isEqualTo("Драма");
    }

    // ============= GET GENRE BY ID TESTS =============

    @Test
    void getGenreById_Success() {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
        when(genreMapper.toDto(genre)).thenReturn(responseDto);

        GenreDto result = genreService.getGenreById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Роман");
        assertThat(result.getId()).isEqualTo(1L);

        verify(genreRepository).findById(1L);
        verify(genreMapper).toDto(genre);
    }

    @Test
    void getGenreById_NotFound_ThrowsEntityNotFound() {
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.getGenreById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genre not found with id: 999");

        verify(genreRepository).findById(999L);
        verify(genreMapper, never()).toDto(any());
    }

    @Test
    void getGenreById_WithNullId_ThrowsException() {
        assertThatThrownBy(() -> genreService.getGenreById(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getGenreById_WithNegativeId_ThrowsException() {
        assertThatThrownBy(() -> genreService.getGenreById(-1L))
                .isInstanceOf(Exception.class);
    }

    // ============= UPDATE GENRE TESTS =============

    @Test
    void updateGenre_Success() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("Роман-эпопея");
        updateDto.setDescription("Масштабное прозаическое произведение");

        Genre updatedGenre = new Genre();
        updatedGenre.setId(1L);
        updatedGenre.setName("Роман-эпопея");
        updatedGenre.setDescription("Масштабное прозаическое произведение");

        GenreDto updatedResponseDto = new GenreDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Роман-эпопея");
        updatedResponseDto.setDescription("Масштабное прозаическое произведение");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
        when(genreRepository.save(any(Genre.class))).thenReturn(updatedGenre);
        when(genreMapper.toDto(updatedGenre)).thenReturn(updatedResponseDto);

        GenreDto result = genreService.updateGenre(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Роман-эпопея");
        assertThat(result.getDescription()).isEqualTo("Масштабное прозаическое произведение");

        verify(genreRepository).findById(1L);
        verify(genreRepository).save(genre);
        verify(genreMapper).toDto(updatedGenre);
    }

    @Test
    void updateGenre_OnlyNameUpdated_WhenDescriptionNull() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("Новый роман");
        updateDto.setDescription(null);

        Genre genreSpy = spy(genre);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        genreService.updateGenre(1L, updateDto);

        verify(genreSpy).setName("Новый роман");
        verify(genreSpy, never()).setDescription(any());
    }

    @Test
    void updateGenre_OnlyDescriptionUpdated_WhenNameSame() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("Роман");
        updateDto.setDescription("Обновленное описание");

        Genre genreSpy = spy(genre);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        genreService.updateGenre(1L, updateDto);

        verify(genreSpy).setName("Роман");
        verify(genreSpy).setDescription("Обновленное описание");
    }

    @Test
    void updateGenre_WithNullDescription_DoesNotUpdateDescription() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("Обновленный роман");
        updateDto.setDescription(null);

        Genre genreSpy = spy(genre);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        genreService.updateGenre(1L, updateDto);

        verify(genreSpy).setName("Обновленный роман");
        verify(genreSpy, never()).setDescription(any());
    }

    @Test
    void updateGenre_WithEmptyDescription_UpdatesDescription() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("Роман");
        updateDto.setDescription("");

        Genre genreSpy = spy(genre);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        genreService.updateGenre(1L, updateDto);

        verify(genreSpy).setName("Роман");
        verify(genreSpy).setDescription("");
    }

    @Test
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

    @Test
    void updateGenre_WithNullName_UpdatesToNull() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName(null);
        updateDto.setDescription("Новое описание");

        Genre genreSpy = spy(genre);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        genreService.updateGenre(1L, updateDto);

        verify(genreSpy).setName(null);
        verify(genreSpy).setDescription("Новое описание");
    }

    @Test
    void updateGenre_WithEmptyName_UpdatesToEmpty() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("");
        updateDto.setDescription("Описание");

        Genre genreSpy = spy(genre);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        genreService.updateGenre(1L, updateDto);

        verify(genreSpy).setName("");
        verify(genreSpy).setDescription("Описание");
    }

    @Test
    void updateGenre_WithAllFieldsNull_UpdatesNameToNull() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName(null);
        updateDto.setDescription(null);

        Genre genreSpy = spy(genre);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        genreService.updateGenre(1L, updateDto);

        verify(genreSpy).setName(null);
        verify(genreSpy, never()).setDescription(any());
    }

    @Test
    void updateGenre_WithSpecialCharactersInName_Success() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("Научная фантастика & Фэнтези");
        updateDto.setDescription("Жанры, основанные на воображении");

        Genre updatedGenre = new Genre();
        updatedGenre.setId(1L);
        updatedGenre.setName("Научная фантастика & Фэнтези");
        updatedGenre.setDescription("Жанры, основанные на воображении");

        GenreDto updatedResponseDto = new GenreDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Научная фантастика & Фэнтези");
        updatedResponseDto.setDescription("Жанры, основанные на воображении");

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
        when(genreRepository.save(any(Genre.class))).thenReturn(updatedGenre);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(updatedResponseDto);

        GenreDto result = genreService.updateGenre(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Научная фантастика & Фэнтези");
    }

    // ============= DELETE GENRE TESTS =============

    @Test
    void deleteGenre_Success() {
        when(genreRepository.existsById(1L)).thenReturn(true);
        doNothing().when(genreRepository).deleteById(1L);

        genreService.deleteGenre(1L);

        verify(genreRepository).existsById(1L);
        verify(genreRepository).deleteById(1L);
    }

    @Test
    void deleteGenre_NotFound_ThrowsEntityNotFound() {
        when(genreRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> genreService.deleteGenre(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Genre not found with id: 999");

        verify(genreRepository).existsById(999L);
        verify(genreRepository, never()).deleteById(any());
    }

    @Test
    void deleteGenre_WithNullId_ThrowsException() {
        assertThatThrownBy(() -> genreService.deleteGenre(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void deleteGenre_WithNegativeId_ThrowsException() {
        assertThatThrownBy(() -> genreService.deleteGenre(-1L))
                .isInstanceOf(Exception.class);
    }

    // ============= EDGE CASES AND ADDITIONAL TESTS =============

    @Test
    void createGenre_WithDuplicateName_ShouldWork() {
        // Жанры могут иметь одинаковые имена? В сервисе нет проверки уникальности
        GenreDto duplicateDto = new GenreDto();
        duplicateDto.setName("Роман");

        Genre duplicateGenre = new Genre();
        duplicateGenre.setId(2L);
        duplicateGenre.setName("Роман");

        when(genreMapper.toEntity(duplicateDto)).thenReturn(duplicateGenre);
        when(genreRepository.save(duplicateGenre)).thenReturn(duplicateGenre);
        when(genreMapper.toDto(duplicateGenre)).thenReturn(responseDto);

        GenreDto result = genreService.createGenre(duplicateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Роман");
    }

    @Test
    void updateGenre_WithoutDescription_ShouldKeepOriginalDescription() {
        GenreDto updateDto = new GenreDto();
        updateDto.setName("Обновленный роман");
        // description не устанавливаем

        Genre genreSpy = spy(genre);

        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreSpy));
        when(genreRepository.save(any(Genre.class))).thenReturn(genreSpy);
        when(genreMapper.toDto(any(Genre.class))).thenReturn(responseDto);

        genreService.updateGenre(1L, updateDto);

        verify(genreSpy).setName("Обновленный роман");
        // description не должен обновляться, так как его нет в DTO
        verify(genreSpy, never()).setDescription(any());
    }

    @Test
    void createGenre_MultipleGenres_Success() {
        List<GenreDto> genres = List.of(
                createGenreDto("Фантастика", "Научно-фантастические произведения"),
                createGenreDto("Детектив", "Расследование преступлений"),
                createGenreDto("Триллер", "Напряженный сюжет")
        );

        for (GenreDto dto : genres) {
            Genre entity = new Genre();
            entity.setName(dto.getName());
            entity.setDescription(dto.getDescription());

            when(genreMapper.toEntity(dto)).thenReturn(entity);
            when(genreRepository.save(entity)).thenReturn(entity);
            when(genreMapper.toDto(entity)).thenReturn(dto);

            GenreDto result = genreService.createGenre(dto);
            assertThat(result.getName()).isEqualTo(dto.getName());
        }
    }

    private GenreDto createGenreDto(String name, String description) {
        GenreDto dto = new GenreDto();
        dto.setName(name);
        dto.setDescription(description);
        return dto;
    }
}