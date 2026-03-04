package com.example.libraryapp.api.mapper;

import com.example.libraryapp.api.dto.AuthorDto;
import com.example.libraryapp.domain.Author;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

    public AuthorDto toDto(Author author) {
        if (author == null) {
            return null;
        }

        AuthorDto dto = new AuthorDto();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setBiography(author.getBiography());
        dto.setBirthDate(author.getBirthDate());
        dto.setNationality(author.getNationality());
        return dto;
    }

    public Author toEntity(AuthorDto dto) {
        if (dto == null) {
            return null;
        }

        Author author = new Author();
        author.setId(dto.getId());
        author.setName(dto.getName());
        author.setBiography(dto.getBiography());
        author.setBirthDate(dto.getBirthDate());
        author.setNationality(dto.getNationality());
        return author;
    }
}