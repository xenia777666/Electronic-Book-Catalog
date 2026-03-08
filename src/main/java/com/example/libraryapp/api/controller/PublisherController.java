package com.example.libraryapp.api.controller;

import com.example.libraryapp.api.dto.PublisherDto;
import com.example.libraryapp.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final PublisherService publisherService;

    @PostMapping
    public ResponseEntity<PublisherDto> createPublisher(@Valid @RequestBody PublisherDto publisherDto) {
        log.info("POST /api/publishers - Creating publisher: {}", publisherDto.getName());
        PublisherDto created = publisherService.createPublisher(publisherDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<PublisherDto>> getAllPublishers() {
        log.info("GET /api/publishers");
        List<PublisherDto> publishers = publisherService.getAllPublishers();
        return ResponseEntity.ok(publishers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublisherDto> getPublisherById(@PathVariable Long id) {
        log.info("GET /api/publishers/{}", id);
        PublisherDto publisher = publisherService.getPublisherById(id);
        return ResponseEntity.ok(publisher);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublisherDto> updatePublisher(
            @PathVariable Long id,
            @Valid @RequestBody PublisherDto publisherDto) {
        log.info("PUT /api/publishers/{} - Updating publisher", id);
        PublisherDto updated = publisherService.updatePublisher(id, publisherDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublisher(@PathVariable Long id) {
        log.info("DELETE /api/publishers/{}", id);
        publisherService.deletePublisher(id);
        return ResponseEntity.noContent().build();
    }
}