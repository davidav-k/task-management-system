package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.comment.CommentRq;
import com.example.taskmanagementsystem.dto.comment.CommentRqToCommentConverter;
import com.example.taskmanagementsystem.entity.Comment;
import com.example.taskmanagementsystem.service.CommentService;
import com.example.taskmanagementsystem.util.DBDataInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "test")
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    DBDataInitializer dbDataInitializer;
    @MockBean
    CommentRqToCommentConverter commentRqToCommentConverter;

    @MockBean
    CommentService commentService;

    List<Comment> comments;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {
        Comment c1 = Comment.builder().id(1L).comment("Comment1").build();
        Comment c2 = Comment.builder().id(2L).comment("Comment2").build();
        Comment c3 = Comment.builder().id(3L).comment("Comment3").build();
        comments = List.of(c1, c2, c3);
    }

    @AfterEach
    void tearDown() {
    }
//
//    @Test
//    void testFindByIdSuccess() throws Exception {
//
//        given(commentService.findById(1L)).willReturn(comments.get(0));
//
//        this.mockMvc.perform(get(baseUrl + "/comment/1").accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Find one success"))
//                .andExpect(jsonPath("$.data").exists())
//                .andExpect(jsonPath("$.data.comment").value("Comment1"));
//    }
//
//    @Test
//    void testFindByIdFail() throws Exception {
//
//        given(commentService.findById(1L)).willThrow(new EntityNotFoundException("Comment with id 1 not found"));
//
//        this.mockMvc.perform(get(baseUrl + "/comment/1").accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(false))
//                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
//                .andExpect(jsonPath("$.message").value("Comment with id 1 not found"))
//                .andExpect(jsonPath("$.data").doesNotExist());
//    }
//
//    @Test
//    void testFindAllSuccess() throws Exception {
//
//        given(commentService.findAll()).willReturn(comments);
//
//        mockMvc.perform(get(baseUrl + "/comment").accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Find all success"))
//                .andExpect(jsonPath("$.data[0].comment").value("Comment1"))
//                .andExpect(jsonPath("$.data[1].comment").value("Comment2"))
//                .andExpect(jsonPath("$.data[2].comment").value("Comment3"))
//                .andExpect(jsonPath("$.data", Matchers.hasSize(3)));
//    }
//
//    @Test
//    void testCreateCommentSuccess() throws Exception {
//
//        CommentRq rq = new CommentRq("Comment1", 1L, 1L);
//        given(commentRqToCommentConverter.convert(rq)).willReturn(comments.get(0));
//        given(commentService.create(any(Comment.class))).willReturn(comments.get(0));
//
//        this.mockMvc.perform(post(baseUrl + "/comment")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(rq))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Create success"))
//                .andExpect(jsonPath("$.data.comment").exists())
//                .andExpect(jsonPath("$.data.comment").value("Comment1"));
//    }
//
//    @Test
//    void testCreateCommentFail() throws Exception {
//        CommentRq rq = new CommentRq("", null, null);
//
//        this.mockMvc.perform(post(baseUrl + "/comment")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(rq))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
//                .andExpect(jsonPath("$.flag").value(false))
//                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
//                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
//                .andExpect(jsonPath("$.data").exists())
//                .andExpect(jsonPath("$.data.comment").value("Length must be from 3 to 30"))
//                .andExpect(jsonPath("$.data.authorId").value("author id required"))
//                .andExpect(jsonPath("$.data.taskId").value("task id required"));
//    }
//
//    @Test
//    void testUpdateCommentSuccess() throws Exception {
//        Instant createAt = Instant.now();
//        Comment updatedComment = Comment.builder()
//                .id(1L)
//                .comment("CommentUp")
//                .author(null)
//                .task(null)
//                .createAt(createAt)
//                .build();
//        CommentRq rq = new CommentRq("CommentUp", 1L, 1L);
//        given(commentRqToCommentConverter.convert(rq)).willReturn(updatedComment);
//        given(commentService.update(1L, updatedComment)).willReturn(updatedComment);
//
//        this.mockMvc.perform(put(baseUrl + "/comment/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(rq))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Update success"))
//                .andExpect(jsonPath("$.data.comment").value("CommentUp"));
//    }
//
//    @Test
//    void testUpdateCommentFail() throws Exception {
//
//        CommentRq rq = new CommentRq("", null, null);
//
//        this.mockMvc.perform(put(baseUrl + "/comment/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(rq))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(false))
//                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
//                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
//                .andExpect(jsonPath("$.data").exists())
//                .andExpect(jsonPath("$.data.comment").value("Length must be from 3 to 30"))
//                .andExpect(jsonPath("$.data.authorId").value("author id required"))
//                .andExpect(jsonPath("$.data.taskId").value("task id required"));
//    }
//
//    @Test
//    void testDeleteByIdSuccess() throws Exception {
//
//        doNothing().when(commentService).deleteById(1L);
//
//        this.mockMvc.perform(delete(baseUrl + "/comment/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Delete success"))
//                .andExpect(jsonPath("$.data").isEmpty());
//
//    }
//
//    @Test
//    void testDeleteByIdFail() throws Exception {
//
//        doThrow(new EntityNotFoundException("comment not found")).when(commentService).deleteById(1L);
//
//        this.mockMvc.perform(delete(baseUrl + "/comment/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(false))
//                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
//                .andExpect(jsonPath("$.message").value("comment not found"))
//                .andExpect(jsonPath("$.data").isEmpty());
//
//    }
}