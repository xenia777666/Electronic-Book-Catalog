package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.PublisherDto;
import com.example.libraryapp.api.mapper.PublisherMapper;
import com.example.libraryapp.domain.Publisher;
import com.example.libraryapp.repository.PublisherRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Transactional
    public PublisherDto createPublisher(PublisherDto publisherDto) {
        log.info("Creating publisher: {}", publisherDto.getName());
        Publisher publisher = publisherMapper.toEntity(publisherDto);
        Publisher savedPublisher = publisherRepository.save(publisher);
        return publisherMapper.toDto(savedPublisher);
    }

    public List<PublisherDto> getAllPublishers() {
        log.debug("Getting all publishers");
        return publisherRepository.findAll()
                .stream()
                .map(publisherMapper::toDto)
                .toList();
    }

    public PublisherDto getPublisherById(Long id) {
        log.debug("Getting publisher by id: {}", id);
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Publisher not found with id: " + id));
        return publisherMapper.toDto(publisher);
    }

    @Transactional
    public PublisherDto updatePublisher(Long id, PublisherDto publisherDto) {
        log.info("Updating publisher with id: {}", id);

        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Publisher not found with id: " + id));

        publisher.setName(publisherDto.getName());
        if (publisherDto.getAddress() != null) {
            publisher.setAddress(publisherDto.getAddress());
        }
        if (publisherDto.getPhone() != null) {
            publisher.setPhone(publisherDto.getPhone());
        }
        if (publisherDto.getEmail() != null) {
            publisher.setEmail(publisherDto.getEmail());
        }

        Publisher updatedPublisher = publisherRepository.save(publisher);
        return publisherMapper.toDto(updatedPublisher);
    }

    @Transactional
    public void deletePublisher(Long id) {
        log.info("Deleting publisher with id: {}", id);

        if (!publisherRepository.existsById(id)) {
            throw new EntityNotFoundException("Publisher not found with id: " + id);
        }
        publisherRepository.deleteById(id);
    }
}