package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.PublisherDto;
import com.example.libraryapp.api.mapper.PublisherMapper;
import com.example.libraryapp.domain.Publisher;
import com.example.libraryapp.repository.PublisherRepository;
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
class PublisherServiceTest {

    @Mock
    private PublisherRepository publisherRepository;

    @Mock
    private PublisherMapper publisherMapper;

    private PublisherService publisherService;

    private PublisherDto publisherDto;
    private Publisher publisher;
    private PublisherDto responseDto;

    @BeforeEach
    void setUp() {
        publisherService = new PublisherService(publisherRepository, publisherMapper);

        publisherDto = new PublisherDto();
        publisherDto.setName("Эксмо");
        publisherDto.setAddress("г. Москва, ул. Публичная, д. 1");
        publisherDto.setPhone("+7 (495) 123-45-67");
        publisherDto.setEmail("info@eksmo.ru");

        publisher = new Publisher();
        publisher.setId(1L);
        publisher.setName("Эксмо");
        publisher.setAddress("г. Москва, ул. Публичная, д. 1");
        publisher.setPhone("+7 (495) 123-45-67");
        publisher.setEmail("info@eksmo.ru");

        responseDto = new PublisherDto();
        responseDto.setId(1L);
        responseDto.setName("Эксмо");
        responseDto.setAddress("г. Москва, ул. Публичная, д. 1");
        responseDto.setPhone("+7 (495) 123-45-67");
        responseDto.setEmail("info@eksmo.ru");
    }

    // ============= CREATE PUBLISHER TESTS =============

    @Test
    void createPublisher_Success() {
        when(publisherMapper.toEntity(publisherDto)).thenReturn(publisher);
        when(publisherRepository.save(publisher)).thenReturn(publisher);
        when(publisherMapper.toDto(publisher)).thenReturn(responseDto);

        PublisherDto result = publisherService.createPublisher(publisherDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Эксмо");
        assertThat(result.getAddress()).isEqualTo("г. Москва, ул. Публичная, д. 1");
        assertThat(result.getPhone()).isEqualTo("+7 (495) 123-45-67");
        assertThat(result.getEmail()).isEqualTo("info@eksmo.ru");

        verify(publisherMapper).toEntity(publisherDto);
        verify(publisherRepository).save(publisher);
        verify(publisherMapper).toDto(publisher);
    }

    @Test
    void createPublisher_WithMinimalData_Success() {
        PublisherDto minimalDto = new PublisherDto();
        minimalDto.setName("АСТ");

        Publisher minimalPublisher = new Publisher();
        minimalPublisher.setId(2L);
        minimalPublisher.setName("АСТ");

        PublisherDto minimalResponse = new PublisherDto();
        minimalResponse.setId(2L);
        minimalResponse.setName("АСТ");

        when(publisherMapper.toEntity(minimalDto)).thenReturn(minimalPublisher);
        when(publisherRepository.save(minimalPublisher)).thenReturn(minimalPublisher);
        when(publisherMapper.toDto(minimalPublisher)).thenReturn(minimalResponse);

        PublisherDto result = publisherService.createPublisher(minimalDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("АСТ");
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getAddress()).isNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getEmail()).isNull();

        verify(publisherMapper).toEntity(minimalDto);
        verify(publisherRepository).save(minimalPublisher);
        verify(publisherMapper).toDto(minimalPublisher);
    }

    @Test
    void createPublisher_WithoutAddress_Success() {
        PublisherDto dtoWithoutAddress = new PublisherDto();
        dtoWithoutAddress.setName("Питер");
        dtoWithoutAddress.setPhone("+7 (812) 123-45-67");
        dtoWithoutAddress.setEmail("info@piter.com");

        Publisher publisherWithoutAddress = new Publisher();
        publisherWithoutAddress.setId(3L);
        publisherWithoutAddress.setName("Питер");
        publisherWithoutAddress.setPhone("+7 (812) 123-45-67");
        publisherWithoutAddress.setEmail("info@piter.com");

        PublisherDto responseWithoutAddress = new PublisherDto();
        responseWithoutAddress.setId(3L);
        responseWithoutAddress.setName("Питер");
        responseWithoutAddress.setPhone("+7 (812) 123-45-67");
        responseWithoutAddress.setEmail("info@piter.com");

        when(publisherMapper.toEntity(dtoWithoutAddress)).thenReturn(publisherWithoutAddress);
        when(publisherRepository.save(publisherWithoutAddress)).thenReturn(publisherWithoutAddress);
        when(publisherMapper.toDto(publisherWithoutAddress)).thenReturn(responseWithoutAddress);

        PublisherDto result = publisherService.createPublisher(dtoWithoutAddress);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Питер");
        assertThat(result.getAddress()).isNull();
        assertThat(result.getPhone()).isEqualTo("+7 (812) 123-45-67");
        assertThat(result.getEmail()).isEqualTo("info@piter.com");
    }

    @Test
    void createPublisher_WithoutPhone_Success() {
        PublisherDto dtoWithoutPhone = new PublisherDto();
        dtoWithoutPhone.setName("Дрофа");
        dtoWithoutPhone.setAddress("г. Москва, ул. Книжная, д. 10");
        dtoWithoutPhone.setEmail("info@drofa.ru");

        Publisher publisherWithoutPhone = new Publisher();
        publisherWithoutPhone.setId(4L);
        publisherWithoutPhone.setName("Дрофа");
        publisherWithoutPhone.setAddress("г. Москва, ул. Книжная, д. 10");
        publisherWithoutPhone.setEmail("info@drofa.ru");

        PublisherDto responseWithoutPhone = new PublisherDto();
        responseWithoutPhone.setId(4L);
        responseWithoutPhone.setName("Дрофа");
        responseWithoutPhone.setAddress("г. Москва, ул. Книжная, д. 10");
        responseWithoutPhone.setEmail("info@drofa.ru");

        when(publisherMapper.toEntity(dtoWithoutPhone)).thenReturn(publisherWithoutPhone);
        when(publisherRepository.save(publisherWithoutPhone)).thenReturn(publisherWithoutPhone);
        when(publisherMapper.toDto(publisherWithoutPhone)).thenReturn(responseWithoutPhone);

        PublisherDto result = publisherService.createPublisher(dtoWithoutPhone);

        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isNull();
    }

    @Test
    void createPublisher_WithoutEmail_Success() {
        PublisherDto dtoWithoutEmail = new PublisherDto();
        dtoWithoutEmail.setName("Просвещение");
        dtoWithoutEmail.setAddress("г. Москва, ул. Школьная, д. 5");
        dtoWithoutEmail.setPhone("+7 (495) 987-65-43");

        Publisher publisherWithoutEmail = new Publisher();
        publisherWithoutEmail.setId(5L);
        publisherWithoutEmail.setName("Просвещение");
        publisherWithoutEmail.setAddress("г. Москва, ул. Школьная, д. 5");
        publisherWithoutEmail.setPhone("+7 (495) 987-65-43");

        PublisherDto responseWithoutEmail = new PublisherDto();
        responseWithoutEmail.setId(5L);
        responseWithoutEmail.setName("Просвещение");
        responseWithoutEmail.setAddress("г. Москва, ул. Школьная, д. 5");
        responseWithoutEmail.setPhone("+7 (495) 987-65-43");

        when(publisherMapper.toEntity(dtoWithoutEmail)).thenReturn(publisherWithoutEmail);
        when(publisherRepository.save(publisherWithoutEmail)).thenReturn(publisherWithoutEmail);
        when(publisherMapper.toDto(publisherWithoutEmail)).thenReturn(responseWithoutEmail);

        PublisherDto result = publisherService.createPublisher(dtoWithoutEmail);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isNull();
    }

    @Test
    void createPublisher_WithPhoneWithoutPlus_Success() {
        PublisherDto dtoWithPhoneNoPlus = new PublisherDto();
        dtoWithPhoneNoPlus.setName("Издательство");
        dtoWithPhoneNoPlus.setPhone("8 (495) 123-45-67");

        Publisher publisherWithPhoneNoPlus = new Publisher();
        publisherWithPhoneNoPlus.setId(6L);
        publisherWithPhoneNoPlus.setName("Издательство");
        publisherWithPhoneNoPlus.setPhone("8 (495) 123-45-67");

        PublisherDto responseWithPhoneNoPlus = new PublisherDto();
        responseWithPhoneNoPlus.setId(6L);
        responseWithPhoneNoPlus.setName("Издательство");
        responseWithPhoneNoPlus.setPhone("8 (495) 123-45-67");

        when(publisherMapper.toEntity(dtoWithPhoneNoPlus)).thenReturn(publisherWithPhoneNoPlus);
        when(publisherRepository.save(publisherWithPhoneNoPlus)).thenReturn(publisherWithPhoneNoPlus);
        when(publisherMapper.toDto(publisherWithPhoneNoPlus)).thenReturn(responseWithPhoneNoPlus);

        PublisherDto result = publisherService.createPublisher(dtoWithPhoneNoPlus);

        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isEqualTo("8 (495) 123-45-67");
    }

    @Test
    void createPublisher_WithVeryLongName_ShouldHandle() {
        String veryLongName = "A".repeat(255);
        PublisherDto longNameDto = new PublisherDto();
        longNameDto.setName(veryLongName);

        Publisher publisherWithLongName = new Publisher();
        publisherWithLongName.setId(7L);
        publisherWithLongName.setName(veryLongName);

        PublisherDto responseWithLongName = new PublisherDto();
        responseWithLongName.setId(7L);
        responseWithLongName.setName(veryLongName);

        when(publisherMapper.toEntity(longNameDto)).thenReturn(publisherWithLongName);
        when(publisherRepository.save(publisherWithLongName)).thenReturn(publisherWithLongName);
        when(publisherMapper.toDto(publisherWithLongName)).thenReturn(responseWithLongName);

        PublisherDto result = publisherService.createPublisher(longNameDto);

        assertThat(result.getName()).isEqualTo(veryLongName);
    }

    // ============= GET ALL PUBLISHERS TESTS =============

    @Test
    void getAllPublishers_Success() {
        when(publisherRepository.findAll()).thenReturn(List.of(publisher));
        when(publisherMapper.toDto(publisher)).thenReturn(responseDto);

        List<PublisherDto> result = publisherService.getAllPublishers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("Эксмо");

        verify(publisherRepository).findAll();
        verify(publisherMapper).toDto(publisher);
    }

    @Test
    void getAllPublishers_EmptyList_ReturnsEmptyList() {
        when(publisherRepository.findAll()).thenReturn(Collections.emptyList());

        List<PublisherDto> result = publisherService.getAllPublishers();

        assertThat(result).isEmpty();
        verify(publisherRepository).findAll();
        verify(publisherMapper, never()).toDto(any());
    }

    @Test
    void getAllPublishers_MultiplePublishers_ReturnsAll() {
        Publisher publisher2 = new Publisher();
        publisher2.setId(2L);
        publisher2.setName("АСТ");
        publisher2.setAddress("г. Москва");

        PublisherDto responseDto2 = new PublisherDto();
        responseDto2.setId(2L);
        responseDto2.setName("АСТ");
        responseDto2.setAddress("г. Москва");

        Publisher publisher3 = new Publisher();
        publisher3.setId(3L);
        publisher3.setName("Питер");
        publisher3.setAddress("г. Санкт-Петербург");

        PublisherDto responseDto3 = new PublisherDto();
        responseDto3.setId(3L);
        responseDto3.setName("Питер");
        responseDto3.setAddress("г. Санкт-Петербург");

        when(publisherRepository.findAll()).thenReturn(List.of(publisher, publisher2, publisher3));
        when(publisherMapper.toDto(publisher)).thenReturn(responseDto);
        when(publisherMapper.toDto(publisher2)).thenReturn(responseDto2);
        when(publisherMapper.toDto(publisher3)).thenReturn(responseDto3);

        List<PublisherDto> result = publisherService.getAllPublishers();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getName()).isEqualTo("Эксмо");
        assertThat(result.get(1).getName()).isEqualTo("АСТ");
        assertThat(result.get(2).getName()).isEqualTo("Питер");
    }

    // ============= GET PUBLISHER BY ID TESTS =============

    @Test
    void getPublisherById_Success() {
        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(publisherMapper.toDto(publisher)).thenReturn(responseDto);

        PublisherDto result = publisherService.getPublisherById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Эксмо");

        verify(publisherRepository).findById(1L);
        verify(publisherMapper).toDto(publisher);
    }

    @Test
    void getPublisherById_NotFound_ThrowsEntityNotFound() {
        when(publisherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisherService.getPublisherById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Publisher not found with id: 999");

        verify(publisherRepository).findById(999L);
        verify(publisherMapper, never()).toDto(any());
    }

    @Test
    void getPublisherById_WithNullId_ThrowsException() {
        assertThatThrownBy(() -> publisherService.getPublisherById(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getPublisherById_WithNegativeId_ThrowsException() {
        assertThatThrownBy(() -> publisherService.getPublisherById(-1L))
                .isInstanceOf(Exception.class);
    }

    // ============= UPDATE PUBLISHER TESTS =============

    @Test
    void updatePublisher_Success() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("Эксмо-АСТ");
        updateDto.setAddress("Обновленный адрес");
        updateDto.setPhone("+7 (495) 999-99-99");
        updateDto.setEmail("new@eksmo-ast.ru");

        Publisher updatedPublisher = new Publisher();
        updatedPublisher.setId(1L);
        updatedPublisher.setName("Эксмо-АСТ");
        updatedPublisher.setAddress("Обновленный адрес");
        updatedPublisher.setPhone("+7 (495) 999-99-99");
        updatedPublisher.setEmail("new@eksmo-ast.ru");

        PublisherDto updatedResponseDto = new PublisherDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Эксмо-АСТ");
        updatedResponseDto.setAddress("Обновленный адрес");
        updatedResponseDto.setPhone("+7 (495) 999-99-99");
        updatedResponseDto.setEmail("new@eksmo-ast.ru");

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(updatedPublisher);
        when(publisherMapper.toDto(updatedPublisher)).thenReturn(updatedResponseDto);

        PublisherDto result = publisherService.updatePublisher(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Эксмо-АСТ");
        assertThat(result.getAddress()).isEqualTo("Обновленный адрес");
        assertThat(result.getPhone()).isEqualTo("+7 (495) 999-99-99");
        assertThat(result.getEmail()).isEqualTo("new@eksmo-ast.ru");

        verify(publisherRepository).findById(1L);
        verify(publisherRepository).save(publisher);
        verify(publisherMapper).toDto(updatedPublisher);
    }

    @Test
    void updatePublisher_OnlyNameUpdated_WhenOtherFieldsNull() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("Новое имя");
        updateDto.setAddress(null);
        updateDto.setPhone(null);
        updateDto.setEmail(null);

        Publisher publisherSpy = spy(publisher);

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisherSpy));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisherSpy);
        when(publisherMapper.toDto(any(Publisher.class))).thenReturn(responseDto);

        publisherService.updatePublisher(1L, updateDto);

        verify(publisherSpy).setName("Новое имя");
        verify(publisherSpy, never()).setAddress(any());
        verify(publisherSpy, never()).setPhone(any());
        verify(publisherSpy, never()).setEmail(any());
    }

    @Test
    void updatePublisher_OnlyAddressUpdated() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("Эксмо");
        updateDto.setAddress("Новый адрес");
        updateDto.setPhone(null);
        updateDto.setEmail(null);

        Publisher publisherSpy = spy(publisher);

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisherSpy));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisherSpy);
        when(publisherMapper.toDto(any(Publisher.class))).thenReturn(responseDto);

        publisherService.updatePublisher(1L, updateDto);

        verify(publisherSpy).setName("Эксмо");
        verify(publisherSpy).setAddress("Новый адрес");
        verify(publisherSpy, never()).setPhone(any());
        verify(publisherSpy, never()).setEmail(any());
    }

    @Test
    void updatePublisher_OnlyPhoneUpdated() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("Эксмо");
        updateDto.setAddress(null);
        updateDto.setPhone("+7 (495) 111-11-11");
        updateDto.setEmail(null);

        Publisher publisherSpy = spy(publisher);

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisherSpy));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisherSpy);
        when(publisherMapper.toDto(any(Publisher.class))).thenReturn(responseDto);

        publisherService.updatePublisher(1L, updateDto);

        verify(publisherSpy).setName("Эксмо");
        verify(publisherSpy, never()).setAddress(any());
        verify(publisherSpy).setPhone("+7 (495) 111-11-11");
        verify(publisherSpy, never()).setEmail(any());
    }

    @Test
    void updatePublisher_OnlyEmailUpdated() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("Эксмо");
        updateDto.setAddress(null);
        updateDto.setPhone(null);
        updateDto.setEmail("new@email.com");

        Publisher publisherSpy = spy(publisher);

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisherSpy));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisherSpy);
        when(publisherMapper.toDto(any(Publisher.class))).thenReturn(responseDto);

        publisherService.updatePublisher(1L, updateDto);

        verify(publisherSpy).setName("Эксмо");
        verify(publisherSpy, never()).setAddress(any());
        verify(publisherSpy, never()).setPhone(any());
        verify(publisherSpy).setEmail("new@email.com");
    }

    @Test
    void updatePublisher_AllFieldsNull_OnlyNameUpdatedToNull() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName(null);
        updateDto.setAddress(null);
        updateDto.setPhone(null);
        updateDto.setEmail(null);

        Publisher publisherSpy = spy(publisher);

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisherSpy));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisherSpy);
        when(publisherMapper.toDto(any(Publisher.class))).thenReturn(responseDto);

        publisherService.updatePublisher(1L, updateDto);

        verify(publisherSpy).setName(null);
        verify(publisherSpy, never()).setAddress(any());
        verify(publisherSpy, never()).setPhone(any());
        verify(publisherSpy, never()).setEmail(any());
    }

    @Test
    void updatePublisher_WithEmptyStrings_UpdatesToEmpty() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("");
        updateDto.setAddress("");
        updateDto.setPhone("");
        updateDto.setEmail("");

        Publisher publisherSpy = spy(publisher);

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisherSpy));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisherSpy);
        when(publisherMapper.toDto(any(Publisher.class))).thenReturn(responseDto);

        publisherService.updatePublisher(1L, updateDto);

        verify(publisherSpy).setName("");
        verify(publisherSpy).setAddress("");
        verify(publisherSpy).setPhone("");
        verify(publisherSpy).setEmail("");
    }

    @Test
    void updatePublisher_NotFound_ThrowsEntityNotFound() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("Новое имя");

        when(publisherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisherService.updatePublisher(999L, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Publisher not found with id: 999");

        verify(publisherRepository).findById(999L);
        verify(publisherRepository, never()).save(any());
    }

    @Test
    void updatePublisher_WithSpecialCharactersInName_Success() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("Издательство №1 \"Лучшее\" & Co.");

        Publisher updatedPublisher = new Publisher();
        updatedPublisher.setId(1L);
        updatedPublisher.setName("Издательство №1 \"Лучшее\" & Co.");

        PublisherDto updatedResponseDto = new PublisherDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setName("Издательство №1 \"Лучшее\" & Co.");

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(updatedPublisher);
        when(publisherMapper.toDto(any(Publisher.class))).thenReturn(updatedResponseDto);

        PublisherDto result = publisherService.updatePublisher(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Издательство №1 \"Лучшее\" & Co.");
    }

    // ============= DELETE PUBLISHER TESTS =============

    @Test
    void deletePublisher_Success() {
        when(publisherRepository.existsById(1L)).thenReturn(true);
        doNothing().when(publisherRepository).deleteById(1L);

        publisherService.deletePublisher(1L);

        verify(publisherRepository).existsById(1L);
        verify(publisherRepository).deleteById(1L);
    }

    @Test
    void deletePublisher_NotFound_ThrowsEntityNotFound() {
        when(publisherRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> publisherService.deletePublisher(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Publisher not found with id: 999");

        verify(publisherRepository).existsById(999L);
        verify(publisherRepository, never()).deleteById(any());
    }

    @Test
    void deletePublisher_WithNullId_ThrowsException() {
        assertThatThrownBy(() -> publisherService.deletePublisher(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void deletePublisher_WithNegativeId_ThrowsException() {
        assertThatThrownBy(() -> publisherService.deletePublisher(-1L))
                .isInstanceOf(Exception.class);
    }

    // ============= EDGE CASES AND ADDITIONAL TESTS =============

    @Test
    void createPublisher_WithMultiplePublishers_Success() {
        List<PublisherDto> publishers = List.of(
                createPublisherDto("АСТ", "Москва", "+7 (495) 111-11-11", "ast@mail.ru"),
                createPublisherDto("Питер", "СПб", "+7 (812) 222-22-22", "piter@mail.ru"),
                createPublisherDto("Дрофа", "Москва", "+7 (495) 333-33-33", "drofa@mail.ru")
        );

        for (PublisherDto dto : publishers) {
            Publisher entity = new Publisher();
            entity.setName(dto.getName());
            entity.setAddress(dto.getAddress());
            entity.setPhone(dto.getPhone());
            entity.setEmail(dto.getEmail());

            when(publisherMapper.toEntity(dto)).thenReturn(entity);
            when(publisherRepository.save(entity)).thenReturn(entity);
            when(publisherMapper.toDto(entity)).thenReturn(dto);

            PublisherDto result = publisherService.createPublisher(dto);
            assertThat(result.getName()).isEqualTo(dto.getName());
        }
    }

    @Test
    void updatePublisher_PartialUpdate_Success() {
        PublisherDto updateDto = new PublisherDto();
        updateDto.setName("Обновленное имя");
        updateDto.setAddress("Обновленный адрес");
        updateDto.setPhone(null);
        updateDto.setEmail("updated@email.com");

        Publisher publisherSpy = spy(publisher);

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisherSpy));
        when(publisherRepository.save(any(Publisher.class))).thenReturn(publisherSpy);
        when(publisherMapper.toDto(any(Publisher.class))).thenReturn(responseDto);

        publisherService.updatePublisher(1L, updateDto);

        verify(publisherSpy).setName("Обновленное имя");
        verify(publisherSpy).setAddress("Обновленный адрес");
        verify(publisherSpy, never()).setPhone(any());
        verify(publisherSpy).setEmail("updated@email.com");
    }

    @Test
    void createPublisher_WithAllNullOptionalFields_Success() {
        PublisherDto dtoWithNulls = new PublisherDto();
        dtoWithNulls.setName("Издательство");
        dtoWithNulls.setAddress(null);
        dtoWithNulls.setPhone(null);
        dtoWithNulls.setEmail(null);

        Publisher publisherWithNulls = new Publisher();
        publisherWithNulls.setId(8L);
        publisherWithNulls.setName("Издательство");

        PublisherDto responseWithNulls = new PublisherDto();
        responseWithNulls.setId(8L);
        responseWithNulls.setName("Издательство");

        when(publisherMapper.toEntity(dtoWithNulls)).thenReturn(publisherWithNulls);
        when(publisherRepository.save(publisherWithNulls)).thenReturn(publisherWithNulls);
        when(publisherMapper.toDto(publisherWithNulls)).thenReturn(responseWithNulls);

        PublisherDto result = publisherService.createPublisher(dtoWithNulls);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Издательство");
        assertThat(result.getAddress()).isNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getEmail()).isNull();
    }

    private PublisherDto createPublisherDto(String name, String address, String phone, String email) {
        PublisherDto dto = new PublisherDto();
        dto.setName(name);
        dto.setAddress(address);
        dto.setPhone(phone);
        dto.setEmail(email);
        return dto;
    }
}