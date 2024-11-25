package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.dto.comment.CommentRq;
import com.example.taskmanagementsystem.dto.comment.CommentRqToCommentConverter;
import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.dto.comment.CommentToCommentRsConverter;
import com.example.taskmanagementsystem.entity.Comment;
import com.example.taskmanagementsystem.repo.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;
    @Mock
    private CommentToCommentRsConverter commentToCommentRsConverter;
    @Mock
    private CommentRqToCommentConverter commentRqToCommentConverter;
    @InjectMocks
    CommentService commentService;

    @Test
    void findById_ShouldReturnComment() {
        Comment comment = Comment.builder().id(1L).comment("Test comment").build();
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        Comment returnedComment = commentService.findById(1L);

        assertThat(returnedComment.getId()).isEqualTo(1L);
        assertThat(returnedComment.getComment()).isEqualTo("Test comment");
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    void  findById_ShouldThrowException_WhenCommentDoesNotExist() {
        given(commentRepository.findById(Mockito.any(Long.class))).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> commentService.findById(1L));

        assertThat(thrown).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Comment with id 1 not found");
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    void findByIdReturnCommentRs_ShouldReturnConvertedCommentRs() {
        Instant instant = Instant.now();
        Comment comment = Comment.builder().id(1L).comment("Test comment").build();
        CommentRs rs = new CommentRs(1L, "Test comment", 1L, 1L, instant);
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(commentToCommentRsConverter.convert(any(Comment.class))).thenReturn(rs);

        CommentRs result = commentService.findByIdReturnCommentRs(1L);

        assertNotNull(result);
        verify(commentRepository).findById(1L);
        verify(commentToCommentRsConverter).convert(comment);
    }

    @Test
    void create_ShouldSaveAndReturnCommentRs() {
        Instant instant = Instant.now();
        Comment comment = Comment.builder().id(1L).comment("Test comment").build();
        CommentRq rq = new CommentRq("Test comment", 1L, 1L);
        CommentRs rs = new CommentRs(1L, "Test comment", 1L, 1L, instant);
        given(commentRqToCommentConverter.convert(rq)).willReturn(comment);
        given(commentRepository.save(comment)).willReturn(comment);
        given(commentToCommentRsConverter.convert(comment)).willReturn(rs);

        CommentRs returnedComment = commentService.create(rq);

        assertThat(returnedComment.id()).isEqualTo(1L);
        assertThat(returnedComment.comment()).isEqualTo("Test comment");
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void create_ShouldThrowException_WhenConversionFails() {
        CommentRq rq = new CommentRq("Test comment", 1L, 1L);
        when(commentRqToCommentConverter.convert(rq)).thenReturn(null);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class, () -> commentService.create(rq));

        assertTrue(exception.getMessage().contains("Conversion failed comment"));
        verify(commentRqToCommentConverter).convert(rq);
        verifyNoInteractions(commentRepository);
        verifyNoInteractions(commentToCommentRsConverter);
    }

    @Test
    void deleteById_ShouldDeleteComment() {
        Comment comment = Comment.builder().id(1L).comment("Test comment").build();
        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        doNothing().when(commentRepository).deleteById(1L);

        commentService.deleteById(1L);

        verify(commentRepository).findById(1L);
        verify(commentRepository).deleteById(1L);
        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_ShouldThrowException_WhenCommentDoesNotExist() {

        given(commentRepository.findById(2L)).willReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class, () -> commentService.deleteById(2L));

        assertEquals("Comment with id 2 not found", exception.getMessage());
        verify(commentRepository).findById(2L);
        verify(commentRepository, Mockito.never()).deleteById(2L);
        verify(commentRepository, times(1)).findById(2L);
    }
}