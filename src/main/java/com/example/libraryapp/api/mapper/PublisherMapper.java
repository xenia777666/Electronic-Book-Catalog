package com.example.libraryapp.api.mapper;

import com.example.libraryapp.api.dto.PublisherDto;
import com.example.libraryapp.domain.Publisher;
import org.springframework.stereotype.Component;

@Component
public class PublisherMapper {

    public PublisherDto toDto(Publisher publisher) {
        if (publisher == null) {
            return null;
        }

        PublisherDto dto = new PublisherDto();
        dto.setId(publisher.getId());
        dto.setName(publisher.getName());
        dto.setAddress(publisher.getAddress());
        dto.setPhone(publisher.getPhone());
        dto.setEmail(publisher.getEmail());
        return dto;
    }

    public Publisher toEntity(PublisherDto dto) {
        if (dto == null) {
            return null;
        }

        Publisher publisher = new Publisher();
        publisher.setId(dto.getId());
        publisher.setName(dto.getName());
        publisher.setAddress(dto.getAddress());
        publisher.setPhone(dto.getPhone());
        publisher.setEmail(dto.getEmail());
        return publisher;
    }
}