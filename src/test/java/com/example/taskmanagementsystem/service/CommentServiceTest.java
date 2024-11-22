package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.entity.Comment;
import com.example.taskmanagementsystem.repo.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = "dev")
class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;
    @InjectMocks
    CommentService commentService;

    private final Comment comment = Comment.builder().id(1L).comment("Test comment").build();

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindByIdSuccess() {

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

        Comment returnedComment = commentService.findById(1L);

        assertThat(returnedComment.getId()).isEqualTo(1L);
        assertThat(returnedComment.getComment()).isEqualTo("Test comment");
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    void testFindByIdFail() {

        given(commentRepository.findById(Mockito.any(Long.class))).willReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> commentService.findById(1L));

        assertThat(thrown).isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Comment with id 1 not found");
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    void testFindAllSuccess() {
        Comment comment1 = Comment.builder().id(2L).comment("Comment1").build();
        List<Comment> comments = List.of(comment, comment1);
        given(commentRepository.findAll()).willReturn(comments);

        List<Comment> returnedComments = commentService.findAll();

        assertThat(returnedComments.get(0).getId()).isEqualTo(1L);
        assertThat(returnedComments.get(0).getComment()).isEqualTo("Test comment");
        assertThat(returnedComments.get(1).getId()).isEqualTo(2L);
        assertThat(returnedComments.get(1).getComment()).isEqualTo("Comment1");
        verify(commentRepository, times(1)).findAll();

    }

    @Test
    void testCreateSuccess() {

        given(commentRepository.save(comment)).willReturn(comment);

        Comment returnedComment = commentService.create(comment);

        assertThat(returnedComment.getId()).isEqualTo(1L);
        assertThat(returnedComment.getComment()).isEqualTo("Test comment");
        verify(commentRepository, times(1)).save(comment);

    }

    @Test
    void testUpdateSuccess() {
        Comment update = Comment.builder().id(1L).comment("Update comment").build();

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        given(commentRepository.save(comment)).willReturn(update);

        Comment returnedComment = commentService.update(1L, update);

        assertThat(returnedComment.getId()).isEqualTo(1L);
        assertThat(returnedComment.getComment()).isEqualTo("Update comment");
        verify(commentRepository, times(1)).save(comment);
    }

    @Test
    void testUpdateFail() {
        Comment update = Comment.builder().comment("Update comment").build();

        given(commentRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            commentService.update(1L, update);
        });

        verify(commentRepository, times(1)).findById(1L);
    }


    @Test
    void deleteByIdSuccess() {

        given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
        doNothing().when(commentRepository).deleteById(1L);

        commentService.deleteById(1L);

        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteByIdFail() {

        given(commentRepository.findById(2L)).willReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> commentService.deleteById(2L));

        verify(commentRepository, times(1)).findById(2L);
    }
}