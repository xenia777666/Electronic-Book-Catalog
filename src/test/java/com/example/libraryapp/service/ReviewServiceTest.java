package com.example.libraryapp.service;

import com.example.libraryapp.api.dto.ReviewDto;
import com.example.libraryapp.api.mapper.ReviewMapper;
import com.example.libraryapp.domain.Book;
import com.example.libraryapp.domain.Review;
import com.example.libraryapp.repository.BookRepository;
import com.example.libraryapp.repository.ReviewRepository;
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
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReviewMapper reviewMapper;

    private ReviewService reviewService;

    private ReviewDto reviewDto;
    private Review review;
    private ReviewDto responseDto;
    private Book book;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, bookRepository, reviewMapper);

        book = new Book();
        book.setId(1L);
        book.setTitle("Война и мир");

        reviewDto = new ReviewDto();
        reviewDto.setReviewerName("Иван Иванов");
        reviewDto.setRating(5);
        reviewDto.setComment("Отличная книга! Очень понравилась.");
        reviewDto.setBookId(1L);

        review = new Review();
        review.setId(1L);
        review.setReviewerName("Иван Иванов");
        review.setRating(5);
        review.setComment("Отличная книга! Очень понравилась.");
        review.setBook(book);

        responseDto = new ReviewDto();
        responseDto.setId(1L);
        responseDto.setReviewerName("Иван Иванов");
        responseDto.setRating(5);
        responseDto.setComment("Отличная книга! Очень понравилась.");
        responseDto.setBookId(1L);
    }

    // ============= CREATE REVIEW TESTS =============

    @Test
    void createReview_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(reviewDto)).thenReturn(review);
        when(reviewRepository.save(review)).thenReturn(review);
        when(reviewMapper.toDto(review)).thenReturn(responseDto);

        ReviewDto result = reviewService.createReview(reviewDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReviewerName()).isEqualTo("Иван Иванов");
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Отличная книга! Очень понравилась.");
        assertThat(result.getBookId()).isEqualTo(1L);

        verify(bookRepository).findById(1L);
        verify(reviewMapper).toEntity(reviewDto);
        verify(reviewRepository).save(review);
        verify(reviewMapper).toDto(review);
    }

    @Test
    void createReview_BookNotFound_ThrowsEntityNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());
        reviewDto.setBookId(999L);

        assertThatThrownBy(() -> reviewService.createReview(reviewDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book not found with id: 999");

        verify(bookRepository).findById(999L);
        verify(reviewRepository, never()).save(any());
        verify(reviewMapper, never()).toEntity(any());
    }

    @Test
    void createReview_WithMinimalData_Success() {
        ReviewDto minimalDto = new ReviewDto();
        minimalDto.setRating(4);
        minimalDto.setBookId(1L);

        Review minimalReview = new Review();
        minimalReview.setId(2L);
        minimalReview.setRating(4);
        minimalReview.setBook(book);

        ReviewDto minimalResponse = new ReviewDto();
        minimalResponse.setId(2L);
        minimalResponse.setRating(4);
        minimalResponse.setBookId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(minimalDto)).thenReturn(minimalReview);
        when(reviewRepository.save(minimalReview)).thenReturn(minimalReview);
        when(reviewMapper.toDto(minimalReview)).thenReturn(minimalResponse);

        ReviewDto result = reviewService.createReview(minimalDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getReviewerName()).isNull();
        assertThat(result.getComment()).isNull();

        verify(bookRepository).findById(1L);
        verify(reviewMapper).toEntity(minimalDto);
        verify(reviewRepository).save(minimalReview);
    }

    @Test
    void createReview_WithoutReviewerName_Success() {
        ReviewDto dtoWithoutReviewer = new ReviewDto();
        dtoWithoutReviewer.setRating(5);
        dtoWithoutReviewer.setComment("Great book!");
        dtoWithoutReviewer.setBookId(1L);

        Review reviewWithoutReviewer = new Review();
        reviewWithoutReviewer.setId(3L);
        reviewWithoutReviewer.setRating(5);
        reviewWithoutReviewer.setComment("Great book!");
        reviewWithoutReviewer.setBook(book);

        ReviewDto responseWithoutReviewer = new ReviewDto();
        responseWithoutReviewer.setId(3L);
        responseWithoutReviewer.setRating(5);
        responseWithoutReviewer.setComment("Great book!");
        responseWithoutReviewer.setBookId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(dtoWithoutReviewer)).thenReturn(reviewWithoutReviewer);
        when(reviewRepository.save(reviewWithoutReviewer)).thenReturn(reviewWithoutReviewer);
        when(reviewMapper.toDto(reviewWithoutReviewer)).thenReturn(responseWithoutReviewer);

        ReviewDto result = reviewService.createReview(dtoWithoutReviewer);

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getReviewerName()).isNull();
    }

    @Test
    void createReview_WithoutComment_Success() {
        ReviewDto dtoWithoutComment = new ReviewDto();
        dtoWithoutComment.setReviewerName("Петр Петров");
        dtoWithoutComment.setRating(3);
        dtoWithoutComment.setBookId(1L);

        Review reviewWithoutComment = new Review();
        reviewWithoutComment.setId(4L);
        reviewWithoutComment.setReviewerName("Петр Петров");
        reviewWithoutComment.setRating(3);
        reviewWithoutComment.setBook(book);

        ReviewDto responseWithoutComment = new ReviewDto();
        responseWithoutComment.setId(4L);
        responseWithoutComment.setReviewerName("Петр Петров");
        responseWithoutComment.setRating(3);
        responseWithoutComment.setBookId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(dtoWithoutComment)).thenReturn(reviewWithoutComment);
        when(reviewRepository.save(reviewWithoutComment)).thenReturn(reviewWithoutComment);
        when(reviewMapper.toDto(reviewWithoutComment)).thenReturn(responseWithoutComment);

        ReviewDto result = reviewService.createReview(dtoWithoutComment);

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(3);
        assertThat(result.getComment()).isNull();
    }

    @Test
    void createReview_WithMinimumRating_Success() {
        ReviewDto minRatingDto = new ReviewDto();
        minRatingDto.setRating(1);
        minRatingDto.setBookId(1L);

        Review minRatingReview = new Review();
        minRatingReview.setId(5L);
        minRatingReview.setRating(1);
        minRatingReview.setBook(book);

        ReviewDto minRatingResponse = new ReviewDto();
        minRatingResponse.setId(5L);
        minRatingResponse.setRating(1);
        minRatingResponse.setBookId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(minRatingDto)).thenReturn(minRatingReview);
        when(reviewRepository.save(minRatingReview)).thenReturn(minRatingReview);
        when(reviewMapper.toDto(minRatingReview)).thenReturn(minRatingResponse);

        ReviewDto result = reviewService.createReview(minRatingDto);

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(1);
    }

    @Test
    void createReview_WithMaximumRating_Success() {
        ReviewDto maxRatingDto = new ReviewDto();
        maxRatingDto.setRating(5);
        maxRatingDto.setBookId(1L);

        Review maxRatingReview = new Review();
        maxRatingReview.setId(6L);
        maxRatingReview.setRating(5);
        maxRatingReview.setBook(book);

        ReviewDto maxRatingResponse = new ReviewDto();
        maxRatingResponse.setId(6L);
        maxRatingResponse.setRating(5);
        maxRatingResponse.setBookId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(maxRatingDto)).thenReturn(maxRatingReview);
        when(reviewRepository.save(maxRatingReview)).thenReturn(maxRatingReview);
        when(reviewMapper.toDto(maxRatingReview)).thenReturn(maxRatingResponse);

        ReviewDto result = reviewService.createReview(maxRatingDto);

        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
    }

    // ============= GET ALL REVIEWS TESTS =============

    @Test
    void getAllReviews_Success() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));
        when(reviewMapper.toDto(review)).thenReturn(responseDto);

        List<ReviewDto> result = reviewService.getAllReviews();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getReviewerName()).isEqualTo("Иван Иванов");

        verify(reviewRepository).findAll();
        verify(reviewMapper).toDto(review);
    }

    @Test
    void getAllReviews_EmptyList_ReturnsEmptyList() {
        when(reviewRepository.findAll()).thenReturn(Collections.emptyList());

        List<ReviewDto> result = reviewService.getAllReviews();

        assertThat(result).isEmpty();
        verify(reviewRepository).findAll();
        verify(reviewMapper, never()).toDto(any());
    }

    @Test
    void getAllReviews_MultipleReviews_ReturnsAll() {
        Review review2 = new Review();
        review2.setId(2L);
        review2.setReviewerName("Мария Сидорова");
        review2.setRating(4);
        review2.setComment("Хорошая книга");
        review2.setBook(book);

        ReviewDto responseDto2 = new ReviewDto();
        responseDto2.setId(2L);
        responseDto2.setReviewerName("Мария Сидорова");
        responseDto2.setRating(4);
        responseDto2.setComment("Хорошая книга");
        responseDto2.setBookId(1L);

        when(reviewRepository.findAll()).thenReturn(List.of(review, review2));
        when(reviewMapper.toDto(review)).thenReturn(responseDto);
        when(reviewMapper.toDto(review2)).thenReturn(responseDto2);

        List<ReviewDto> result = reviewService.getAllReviews();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReviewerName()).isEqualTo("Иван Иванов");
        assertThat(result.get(1).getReviewerName()).isEqualTo("Мария Сидорова");
    }

    // ============= GET REVIEW BY ID TESTS =============

    @Test
    void getReviewById_Success() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewMapper.toDto(review)).thenReturn(responseDto);

        ReviewDto result = reviewService.getReviewById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getReviewerName()).isEqualTo("Иван Иванов");

        verify(reviewRepository).findById(1L);
        verify(reviewMapper).toDto(review);
    }

    @Test
    void getReviewById_NotFound_ThrowsEntityNotFound() {
        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Review not found with id: 999");

        verify(reviewRepository).findById(999L);
        verify(reviewMapper, never()).toDto(any());
    }

    @Test
    void getReviewById_WithNullId_ThrowsException() {
        assertThatThrownBy(() -> reviewService.getReviewById(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void getReviewById_WithNegativeId_ThrowsException() {
        assertThatThrownBy(() -> reviewService.getReviewById(-1L))
                .isInstanceOf(Exception.class);
    }

    // ============= GET REVIEWS BY BOOK ID TESTS =============

    @Test
    void getReviewsByBookId_Success() {
        when(reviewRepository.findByBookId(1L)).thenReturn(List.of(review));
        when(reviewMapper.toDto(review)).thenReturn(responseDto);

        List<ReviewDto> result = reviewService.getReviewsByBookId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookId()).isEqualTo(1L);
        assertThat(result.get(0).getReviewerName()).isEqualTo("Иван Иванов");

        verify(reviewRepository).findByBookId(1L);
        verify(reviewMapper).toDto(review);
    }

    @Test
    void getReviewsByBookId_NoReviews_ReturnsEmptyList() {
        when(reviewRepository.findByBookId(999L)).thenReturn(Collections.emptyList());

        List<ReviewDto> result = reviewService.getReviewsByBookId(999L);

        assertThat(result).isEmpty();
        verify(reviewRepository).findByBookId(999L);
        verify(reviewMapper, never()).toDto(any());
    }

    @Test
    void getReviewsByBookId_MultipleReviews_ReturnsAll() {
        Review review2 = new Review();
        review2.setId(2L);
        review2.setReviewerName("Мария Сидорова");
        review2.setRating(4);
        review2.setComment("Хорошая книга");
        review2.setBook(book);

        ReviewDto responseDto2 = new ReviewDto();
        responseDto2.setId(2L);
        responseDto2.setReviewerName("Мария Сидорова");
        responseDto2.setRating(4);
        responseDto2.setComment("Хорошая книга");
        responseDto2.setBookId(1L);

        when(reviewRepository.findByBookId(1L)).thenReturn(List.of(review, review2));
        when(reviewMapper.toDto(review)).thenReturn(responseDto);
        when(reviewMapper.toDto(review2)).thenReturn(responseDto2);

        List<ReviewDto> result = reviewService.getReviewsByBookId(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getReviewerName()).isEqualTo("Иван Иванов");
        assertThat(result.get(1).getReviewerName()).isEqualTo("Мария Сидорова");
    }

    @Test
    void getReviewsByBookId_WithNullBookId_DoesNotThrowException() {
        // Метод getReviewsByBookId принимает Long, который может быть null
        // Репозиторий вернет пустой список для null bookId
        when(reviewRepository.findByBookId(null)).thenReturn(Collections.emptyList());

        List<ReviewDto> result = reviewService.getReviewsByBookId(null);

        assertThat(result).isEmpty();
        verify(reviewRepository).findByBookId(null);
    }

    // ============= UPDATE REVIEW TESTS =============

    @Test
    void updateReview_Success() {
        ReviewDto updateDto = new ReviewDto();
        updateDto.setReviewerName("Иван Петров");
        updateDto.setRating(4);
        updateDto.setComment("Перечитал, все еще отлично!");

        Review updatedReview = new Review();
        updatedReview.setId(1L);
        updatedReview.setReviewerName("Иван Петров");
        updatedReview.setRating(4);
        updatedReview.setComment("Перечитал, все еще отлично!");
        updatedReview.setBook(book);

        ReviewDto updatedResponseDto = new ReviewDto();
        updatedResponseDto.setId(1L);
        updatedResponseDto.setReviewerName("Иван Петров");
        updatedResponseDto.setRating(4);
        updatedResponseDto.setComment("Перечитал, все еще отлично!");
        updatedResponseDto.setBookId(1L);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);
        when(reviewMapper.toDto(updatedReview)).thenReturn(updatedResponseDto);

        ReviewDto result = reviewService.updateReview(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getReviewerName()).isEqualTo("Иван Петров");
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Перечитал, все еще отлично!");

        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(review);
        verify(reviewMapper).toDto(updatedReview);
    }

    @Test
    void updateReview_OnlyReviewerNameUpdated() {
        ReviewDto updateDto = new ReviewDto();
        updateDto.setReviewerName("Новый reviewer");
        updateDto.setRating(null);
        updateDto.setComment(null);

        Review reviewSpy = spy(review);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewSpy));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewSpy);
        when(reviewMapper.toDto(any(Review.class))).thenReturn(responseDto);

        reviewService.updateReview(1L, updateDto);

        verify(reviewSpy).setReviewerName("Новый reviewer");
        verify(reviewSpy, never()).setRating(any());
        verify(reviewSpy, never()).setComment(any());
    }

    @Test
    void updateReview_OnlyRatingUpdated() {
        ReviewDto updateDto = new ReviewDto();
        updateDto.setReviewerName(null);
        updateDto.setRating(3);
        updateDto.setComment(null);

        Review reviewSpy = spy(review);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewSpy));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewSpy);
        when(reviewMapper.toDto(any(Review.class))).thenReturn(responseDto);

        reviewService.updateReview(1L, updateDto);

        verify(reviewSpy, never()).setReviewerName(any());
        verify(reviewSpy).setRating(3);
        verify(reviewSpy, never()).setComment(any());
    }

    @Test
    void updateReview_OnlyCommentUpdated() {
        ReviewDto updateDto = new ReviewDto();
        updateDto.setReviewerName(null);
        updateDto.setRating(null);
        updateDto.setComment("Новый комментарий");

        Review reviewSpy = spy(review);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewSpy));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewSpy);
        when(reviewMapper.toDto(any(Review.class))).thenReturn(responseDto);

        reviewService.updateReview(1L, updateDto);

        verify(reviewSpy, never()).setReviewerName(any());
        verify(reviewSpy, never()).setRating(any());
        verify(reviewSpy).setComment("Новый комментарий");
    }

    @Test
    void updateReview_AllFieldsNull_NoChanges() {
        ReviewDto updateDto = new ReviewDto();
        updateDto.setReviewerName(null);
        updateDto.setRating(null);
        updateDto.setComment(null);

        Review reviewSpy = spy(review);

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewSpy));
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewSpy);
        when(reviewMapper.toDto(any(Review.class))).thenReturn(responseDto);

        reviewService.updateReview(1L, updateDto);

        verify(reviewSpy, never()).setReviewerName(any());
        verify(reviewSpy, never()).setRating(any());
        verify(reviewSpy, never()).setComment(any());
        verify(reviewRepository).save(reviewSpy);
    }

    @Test
    void updateReview_NotFound_ThrowsEntityNotFound() {
        ReviewDto updateDto = new ReviewDto();
        updateDto.setReviewerName("Новый reviewer");

        when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.updateReview(999L, updateDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Review not found with id: 999");

        verify(reviewRepository).findById(999L);
        verify(reviewRepository, never()).save(any());
    }

    // ============= DELETE REVIEW TESTS =============

    @Test
    void deleteReview_Success() {
        when(reviewRepository.existsById(1L)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(1L);

        reviewService.deleteReview(1L);

        verify(reviewRepository).existsById(1L);
        verify(reviewRepository).deleteById(1L);
    }

    @Test
    void deleteReview_NotFound_ThrowsEntityNotFound() {
        when(reviewRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.deleteReview(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Review not found with id: 999");

        verify(reviewRepository).existsById(999L);
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    void deleteReview_WithNullId_ThrowsException() {
        assertThatThrownBy(() -> reviewService.deleteReview(null))
                .isInstanceOf(Exception.class);
    }

    @Test
    void deleteReview_WithNegativeId_ThrowsException() {
        assertThatThrownBy(() -> reviewService.deleteReview(-1L))
                .isInstanceOf(Exception.class);
    }

    // ============= EDGE CASES AND ADDITIONAL TESTS =============

    @Test
    void createReview_WithVeryLongComment_ShouldHandle() {
        String veryLongComment = "A".repeat(2000);
        ReviewDto longCommentDto = new ReviewDto();
        longCommentDto.setRating(5);
        longCommentDto.setComment(veryLongComment);
        longCommentDto.setBookId(1L);

        Review longCommentReview = new Review();
        longCommentReview.setId(7L);
        longCommentReview.setRating(5);
        longCommentReview.setComment(veryLongComment);
        longCommentReview.setBook(book);

        ReviewDto longCommentResponse = new ReviewDto();
        longCommentResponse.setId(7L);
        longCommentResponse.setRating(5);
        longCommentResponse.setComment(veryLongComment);
        longCommentResponse.setBookId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(longCommentDto)).thenReturn(longCommentReview);
        when(reviewRepository.save(longCommentReview)).thenReturn(longCommentReview);
        when(reviewMapper.toDto(longCommentReview)).thenReturn(longCommentResponse);

        ReviewDto result = reviewService.createReview(longCommentDto);

        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo(veryLongComment);
    }

    @Test
    void createReview_WithVeryLongReviewerName_ShouldHandle() {
        String veryLongName = "A".repeat(100);
        ReviewDto longNameDto = new ReviewDto();
        longNameDto.setReviewerName(veryLongName);
        longNameDto.setRating(5);
        longNameDto.setBookId(1L);

        Review longNameReview = new Review();
        longNameReview.setId(8L);
        longNameReview.setReviewerName(veryLongName);
        longNameReview.setRating(5);
        longNameReview.setBook(book);

        ReviewDto longNameResponse = new ReviewDto();
        longNameResponse.setId(8L);
        longNameResponse.setReviewerName(veryLongName);
        longNameResponse.setRating(5);
        longNameResponse.setBookId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(longNameDto)).thenReturn(longNameReview);
        when(reviewRepository.save(longNameReview)).thenReturn(longNameReview);
        when(reviewMapper.toDto(longNameReview)).thenReturn(longNameResponse);

        ReviewDto result = reviewService.createReview(longNameDto);

        assertThat(result).isNotNull();
        assertThat(result.getReviewerName()).isEqualTo(veryLongName);
    }

    @Test
    void createReview_WithSpecialCharactersInComment_Success() {
        String specialComment = "Отличная книга! @#$%^&*()_+{}[]|\\:;\"'<>,.?/~`";
        ReviewDto specialCommentDto = new ReviewDto();
        specialCommentDto.setRating(5);
        specialCommentDto.setComment(specialComment);
        specialCommentDto.setBookId(1L);

        Review specialCommentReview = new Review();
        specialCommentReview.setId(9L);
        specialCommentReview.setRating(5);
        specialCommentReview.setComment(specialComment);
        specialCommentReview.setBook(book);

        ReviewDto specialCommentResponse = new ReviewDto();
        specialCommentResponse.setId(9L);
        specialCommentResponse.setRating(5);
        specialCommentResponse.setComment(specialComment);
        specialCommentResponse.setBookId(1L);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(reviewMapper.toEntity(specialCommentDto)).thenReturn(specialCommentReview);
        when(reviewRepository.save(specialCommentReview)).thenReturn(specialCommentReview);
        when(reviewMapper.toDto(specialCommentReview)).thenReturn(specialCommentResponse);

        ReviewDto result = reviewService.createReview(specialCommentDto);

        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo(specialComment);
    }
}