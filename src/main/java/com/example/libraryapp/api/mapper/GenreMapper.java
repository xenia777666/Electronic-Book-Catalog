package com.example.libraryapp.api.mapper;

import com.example.libraryapp.api.dto.GenreDto;
import com.example.libraryapp.domain.Genre;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {

    public GenreDto toDto(Genre genre) {
        if (genre == null) {
            return null;
        }

        GenreDto dto = new GenreDto();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        dto.setDescription(genre.getDescription());
        return dto;
    }

    public Genre toEntity(GenreDto dto) {
        if (dto == null) {
            return null;
        }

        Genre genre = new Genre();
        genre.setId(dto.getId());
        genre.setName(dto.getName());
        genre.setDescription(dto.getDescription());
        return genre;
    }
}