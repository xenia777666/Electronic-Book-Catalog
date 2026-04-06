package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.AuthorDto;
import com.example.libraryapp.api.mapper.AuthorMapper;
import com.example.libraryapp.domain.Author;
import com.example.libraryapp.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorMapper authorMapper;

    private AuthorService authorService;

    private AuthorDto authorDto;
    private Author author;
    private AuthorDto responseDto;

    @BeforeEach
    void setUp() {
        authorService = new AuthorService(authorRepository, authorMapper);

        authorDto = new AuthorDto();
        authorDto.setName("Лев Толстой");
        authorDto.setBiography("Великий русский писатель");
        authorDto.setBirthDate(LocalDate.of(1828, 9, 9));

        author = new Author();
        author.setId(1L);
        author.setName("Лев Толстой");
        author.setBiography("Великий русский писатель");
        author.setBirthDate(LocalDate.of(1828, 9, 9));

        responseDto = new AuthorDto();
        responseDto.setId(1L);
        responseDto.setName("Лев Толстой");
        responseDto.setBiography("Великий русский писатель");
        responseDto.setBirthDate(LocalDate.of(1828, 9, 9));
    }

    @Test
    void createAuthor_Success() {
        when(authorRepository.findByName(authorDto.getName())).thenReturn(Optional.empty());
        when(authorMapper.toEntity(authorDto)).thenReturn(author);
        when(authorRepository.save(author)).thenReturn(author);
        when(authorMapper.toDto(author)).thenReturn(responseDto);

        AuthorDto result = authorService.createAuthor(authorDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Лев Толстой");
        assertThat(result.getId()).isEqualTo(1L);
        verify(authorRepository).findByName(authorDto.getName());
        verify(authorMapper).toEntity(authorDto);
        verify(authorRepository).save(author);
        verify(authorMapper).toDto(author);
    }

    @Test
    void createAuthor_NameExists_ThrowsConflict() {
        when(authorRepository.findByName(authorDto.getName())).thenReturn(Optional.of(author));

        assertThatThrownBy(() -> authorService.createAuthor(authorDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Автор с именем Лев Толстой уже существует");

        verify(authorRepository).findByName(authorDto.getName());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void createAuthor_DataIntegrityViolation_ThrowsConflict() {
        when(authorRepository.findByName(authorDto.getName())).thenReturn(Optional.empty());
        when(authorMapper.toEntity(authorDto)).thenReturn(author);
        when(authorRepository.save(author)).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> authorService.createAuthor(authorDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Не удалось создать автора");

        verify(authorRepository).findByName(authorDto.getName());
        verify(authorMapper).toEntity(authorDto);
        verify(authorRepository).save(author);
    }


    @Test
    void getAllAuthors_Success() {
        when(authorRepository.findAll()).thenReturn(List.of(author));
        when(authorMapper.toDto(author)).thenReturn(responseDto);

        List<AuthorDto> result = authorService.getAllAuthors();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Лев Толстой");
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(authorRepository).findAll();
        verify(authorMapper).toDto(author);
    }

    @Test
    void getAllAuthors_EmptyList_ReturnsEmptyList() {
        when(authorRepository.findAll()).thenReturn(Collections.emptyList());

        List<AuthorDto> result = authorService.getAllAuthors();

        assertThat(result).isEmpty();
        verify(authorRepository).findAll();
        verify(authorMapper, never()).toDto(any());
    }

    @Test
    void getAllAuthors_MultipleAuthors_ReturnsAll() {
        Author author2 = new Author();
        author2.setId(2L);
        author2.setName("Фёдор Достоевский");
        author2.setBiography("Русский писатель");
        author2.setBirthDate(LocalDate.of(1821, 11, 11));

        AuthorDto responseDto2 = new AuthorDto();
        responseDto2.setId(2L);
        responseDto2.setName("Фёдор Достоевский");
        responseDto2.setBiography("Русский писатель");
        responseDto2.setBirthDate(LocalDate.of(1821, 11, 11));

        when(authorRepository.findAll()).thenReturn(List.of(author, author2));
        when(authorMapper.toDto(author)).thenReturn(responseDto);
        when(authorMapper.toDto(author2)).thenReturn(responseDto2);

        List<AuthorDto> result = authorService.getAllAuthors();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Лев Толстой");
        assertThat(result.get(1).getName()).isEqualTo("Фёдор Достоевский");
    }

        @Test
    void getAuthorById_Success() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorMapper.toDto(author)).thenReturn(responseDto);

        AuthorDto result = authorService.getAuthorById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Лев Толстой");
        assertThat(result.getId()).isEqualTo(1L);

        verify(authorRepository).findById(1L);
        verify(authorMapper).toDto(author);
    }

    @Test
    void getAuthorById_NotFound_ThrowsEntityNotFound() {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.getAuthorById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author not found with id: 999");

        verify(authorRepository).findById(999L);
        verify(authorMapper, never()).toDto(any());
    }

        @Test
    void updateAuthor_Success() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("Лев Николаевич Толстой");
        updateDto.setBiography("Обновленная биография");
        updateDto.setBirthDate(LocalDate.of(1828, 9, 9));

        Author updatedAuthor = new Author();
        updatedAuthor.setId(1L);
        updatedAuthor.setName("Лев Николаевич Толстой");
        updatedAuthor.setBiography("Обновленная биография");
        updatedAuthor.setBirthDate(LocalDate.of(1828, 9, 9));

        AuthorDto updatedResponseDto = new AuthorDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Лев Николаевич Толстой");
        updatedResponseDto.setBiography("Обновленная биография");
        updatedResponseDto.setBirthDate(LocalDate.of(1828, 9, 9));

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.findByName(updateDto.getName())).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);
        when(authorMapper.toDto(updatedAuthor)).thenReturn(updatedResponseDto);

        AuthorDto result = authorService.updateAuthor(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Лев Николаевич Толстой");
        assertThat(result.getBiography()).isEqualTo("Обновленная биография");

        verify(authorRepository).findById(1L);
        verify(authorRepository).findByName(updateDto.getName());
        verify(authorRepository).save(author);
        verify(authorMapper).toDto(updatedAuthor);
    }

    @Test
    void updateAuthor_SameName_NoDuplicateCheck() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("Лев Толстой");
        updateDto.setBiography("Обновленная биография");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);
        when(authorMapper.toDto(author)).thenReturn(responseDto);

        AuthorDto result = authorService.updateAuthor(1L, updateDto);

        assertThat(result).isNotNull();
        verify(authorRepository).findById(1L);
        verify(authorRepository, never()).findByName(anyString());
        verify(authorRepository).save(author);
    }

    @Test
    void updateAuthor_NameExists_ThrowsConflict() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("Фёдор Достоевский");

        Author existingAuthorWithName = new Author();
        existingAuthorWithName.setId(2L);
        existingAuthorWithName.setName("Фёдор Достоевский");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.findByName(updateDto.getName())).thenReturn(Optional.of(existingAuthorWithName));

        assertThatThrownBy(() -> authorService.updateAuthor(1L, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Автор с именем Фёдор Достоевский уже существует");

        verify(authorRepository).findById(1L);
        verify(authorRepository).findByName(updateDto.getName());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void updateAuthor_NotFound_ThrowsEntityNotFound() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("Новое имя");

        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.updateAuthor(999L, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author not found with id: 999");

        verify(authorRepository).findById(999L);
        verify(authorRepository, never()).findByName(any());
        verify(authorRepository, never()).save(any());
    }

    @Test
    void updateAuthor_DataIntegrityViolation_ThrowsConflict() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("Лев Николаевич Толстой");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.findByName(updateDto.getName())).thenReturn(Optional.empty());
        when(authorRepository.save(author)).thenThrow(new DataIntegrityViolationException("duplicate key"));

        assertThatThrownBy(() -> authorService.updateAuthor(1L, updateDto))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.CONFLICT)
                .hasMessageContaining("Не удалось обновить автора");

        verify(authorRepository).findById(1L);
        verify(authorRepository).findByName(updateDto.getName());
        verify(authorRepository).save(author);
    }

    @Test
    void updateAuthor_WithNullBiography_DoesNotUpdateBiographyButUpdatesOthers() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("Лев Николаевич Толстой");
        updateDto.setBiography(null);
        updateDto.setBirthDate(LocalDate.of(1828, 9, 9));

        Author authorSpy = spy(author);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorSpy));
        when(authorRepository.findByName(updateDto.getName())).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(authorSpy);
        when(authorMapper.toDto(any(Author.class))).thenReturn(responseDto);

        authorService.updateAuthor(1L, updateDto);

        verify(authorSpy).setName("Лев Николаевич Толстой");
        verify(authorSpy, never()).setBiography(any());
        verify(authorSpy).setBirthDate(LocalDate.of(1828, 9, 9));
    }

    @Test
    void updateAuthor_WithNullBirthDate_DoesNotUpdateBirthDateButUpdatesOthers() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("Лев Николаевич Толстой");
        updateDto.setBiography("Обновленная биография");
        updateDto.setBirthDate(null);

        Author authorSpy = spy(author);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorSpy));
        when(authorRepository.findByName(updateDto.getName())).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(authorSpy);
        when(authorMapper.toDto(any(Author.class))).thenReturn(responseDto);

        authorService.updateAuthor(1L, updateDto);

        verify(authorSpy).setName("Лев Николаевич Толстой");
        verify(authorSpy).setBiography("Обновленная биография");
        verify(authorSpy, never()).setBirthDate(any());
    }

    @Test
    void updateAuthor_WithNullName_DoesNotUpdate() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName(null);
        updateDto.setBiography("Обновленная биография");
        updateDto.setBirthDate(LocalDate.of(1828, 9, 9));

        Author authorSpy = spy(author);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorSpy));
        when(authorRepository.save(any(Author.class))).thenReturn(authorSpy);
        when(authorMapper.toDto(any(Author.class))).thenReturn(responseDto);

        authorService.updateAuthor(1L, updateDto);

        verify(authorSpy).setName(null);
        verify(authorSpy).setBiography("Обновленная биография");
        verify(authorSpy).setBirthDate(LocalDate.of(1828, 9, 9));
    }

    @Test
    void updateAuthor_WithAllFieldsNull_UpdatesNameToNull() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName(null);
        updateDto.setBiography(null);
        updateDto.setBirthDate(null);

        Author authorSpy = spy(author);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorSpy));
        when(authorRepository.save(any(Author.class))).thenReturn(authorSpy);
        when(authorMapper.toDto(any(Author.class))).thenReturn(responseDto);

        authorService.updateAuthor(1L, updateDto);

        verify(authorSpy).setName(null);
        verify(authorSpy, never()).setBiography(any());
        verify(authorSpy, never()).setBirthDate(any());
    }


    @Test
    void deleteAuthor_Success() {
        when(authorRepository.existsById(1L)).thenReturn(true);
        doNothing().when(authorRepository).deleteById(1L);

        authorService.deleteAuthor(1L);

        verify(authorRepository).existsById(1L);
        verify(authorRepository).deleteById(1L);
    }

    @Test
    void deleteAuthor_NotFound_ThrowsEntityNotFound() {
        when(authorRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> authorService.deleteAuthor(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Author not found with id: 999");

        verify(authorRepository).existsById(999L);
        verify(authorRepository, never()).deleteById(any());
    }


    @Test
    void searchAuthorsByName_Success() {
        when(authorRepository.searchByName("толст")).thenReturn(List.of(author));
        when(authorMapper.toDto(author)).thenReturn(responseDto);

        List<AuthorDto> result = authorService.searchAuthorsByName("толст");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Лев Толстой");
        assertThat(result.get(0).getId()).isEqualTo(1L);

        verify(authorRepository).searchByName("толст");
        verify(authorMapper).toDto(author);
    }

    @Test
    void searchAuthorsByName_EmptyResult_ReturnsEmptyList() {
        when(authorRepository.searchByName("несуществующий")).thenReturn(Collections.emptyList());

        List<AuthorDto> result = authorService.searchAuthorsByName("несуществующий");

        assertThat(result).isEmpty();
        verify(authorRepository).searchByName("несуществующий");
        verify(authorMapper, never()).toDto(any());
    }

    @Test
    void searchAuthorsByName_PartialMatch_ReturnsMatchingAuthors() {
        Author author2 = new Author();
        author2.setId(2L);
        author2.setName("Антон Чехов");

        AuthorDto responseDto2 = new AuthorDto();
        responseDto2.setId(2L);
        responseDto2.setName("Антон Чехов");

        when(authorRepository.searchByName("чех")).thenReturn(List.of(author2));
        when(authorMapper.toDto(author2)).thenReturn(responseDto2);

        List<AuthorDto> result = authorService.searchAuthorsByName("чех");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Антон Чехов");
    }

    @Test
    void searchAuthorsByName_WithEmptyString_ReturnsEmptyList() {
        when(authorRepository.searchByName("")).thenReturn(Collections.emptyList());

        List<AuthorDto> result = authorService.searchAuthorsByName("");

        assertThat(result).isEmpty();
        verify(authorRepository).searchByName("");
    }


    @Test
    void createAuthor_WithMinimalData_Success() {
        AuthorDto minimalDto = new AuthorDto();
        minimalDto.setName("Минимальный автор");

        Author minimalAuthor = new Author();
        minimalAuthor.setId(3L);
        minimalAuthor.setName("Минимальный автор");

        AuthorDto minimalResponse = new AuthorDto();
        minimalResponse.setId(3L);
        minimalResponse.setName("Минимальный автор");

        when(authorRepository.findByName(minimalDto.getName())).thenReturn(Optional.empty());
        when(authorMapper.toEntity(minimalDto)).thenReturn(minimalAuthor);
        when(authorRepository.save(minimalAuthor)).thenReturn(minimalAuthor);
        when(authorMapper.toDto(minimalAuthor)).thenReturn(minimalResponse);

        AuthorDto result = authorService.createAuthor(minimalDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Минимальный автор");
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getBiography()).isNull();
        assertThat(result.getBirthDate()).isNull();
    }

    @Test
    void updateAuthor_WithSpecialCharactersInName_Success() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("Дж. К. Роулинг (J.K. Rowling)");

        Author updatedAuthor = new Author();
        updatedAuthor.setId(1L);
        updatedAuthor.setName("Дж. К. Роулинг (J.K. Rowling)");

        AuthorDto updatedResponseDto = new AuthorDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Дж. К. Роулинг (J.K. Rowling)");

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(authorRepository.findByName(updateDto.getName())).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);
        when(authorMapper.toDto(any(Author.class))).thenReturn(updatedResponseDto);

        AuthorDto result = authorService.updateAuthor(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Дж. К. Роулинг (J.K. Rowling)");
    }

    @Test
    void createAuthor_WithVeryLongName_ShouldHandle() {
        String veryLongName = "A".repeat(1000);
        authorDto.setName(veryLongName);

        Author authorWithLongName = new Author();
        authorWithLongName.setId(1L);
        authorWithLongName.setName(veryLongName);

        AuthorDto responseWithLongName = new AuthorDto();
        responseWithLongName.setId(1L);
        responseWithLongName.setName(veryLongName);

        when(authorRepository.findByName(veryLongName)).thenReturn(Optional.empty());
        when(authorMapper.toEntity(authorDto)).thenReturn(authorWithLongName);
        when(authorRepository.save(authorWithLongName)).thenReturn(authorWithLongName);
        when(authorMapper.toDto(authorWithLongName)).thenReturn(responseWithLongName);

        AuthorDto result = authorService.createAuthor(authorDto);

        assertThat(result.getName()).isEqualTo(veryLongName);
    }

    @Test
    void updateAuthor_WithEmptyName_UpdatesToEmpty() {
        AuthorDto updateDto = new AuthorDto();
        updateDto.setName("");
        updateDto.setBiography("Новая биография");

        Author authorSpy = spy(author);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(authorSpy));
        when(authorRepository.findByName("")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenReturn(authorSpy);
        when(authorMapper.toDto(any(Author.class))).thenReturn(responseDto);

        authorService.updateAuthor(1L, updateDto);

        verify(authorSpy).setName("");
        verify(authorSpy).setBiography("Новая биография");
    }
}