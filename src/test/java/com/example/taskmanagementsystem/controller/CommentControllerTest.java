package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.comment.CommentRq;
import com.example.taskmanagementsystem.dto.comment.CommentRqToCommentConverter;
import com.example.taskmanagementsystem.dto.comment.CommentRs;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindByIdSuccess() throws Exception {
        Comment comment = Comment.builder().id(1L).comment("Test comment").build();
        given(commentService.findById(1L)).willReturn(comment);

        this.mockMvc.perform(get(baseUrl + "/comment/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find one success"));
    }

    @Test
    void testFindByIdFail() throws Exception {

        given(commentService.findByIdReturnCommentRs(anyLong())).willThrow(new EntityNotFoundException("Comment with id 15 not found"));

        this.mockMvc.perform(get(baseUrl + "/comment/15").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Comment with id 15 not found"));
    }

    @Test
    void testCreateCommentSuccess() throws Exception {
        Instant instant = Instant.now();
        Comment comment = Comment.builder().id(1L).comment("Test comment").build();
        CommentRs commentRs = new CommentRs(1L, "Test comment", 1L, 1L, instant);
        CommentRq rq = new CommentRq("Test comment", 1L, 1L);
        given(commentRqToCommentConverter.convert(rq)).willReturn(comment);
        given(commentService.create(any(CommentRq.class))).willReturn(commentRs);

        this.mockMvc.perform(post(baseUrl + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Comment created"))
                .andExpect(jsonPath("$.data.comment").exists())
                .andExpect(jsonPath("$.data.comment").value("Test comment"));
    }

    @Test
    void testCreateCommentFail() throws Exception {
        CommentRq rq = new CommentRq("", null, null);

        this.mockMvc.perform(post(baseUrl + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").value(Matchers.any(String.class)))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.comment").value("Length must be from 3 to 30"))
                .andExpect(jsonPath("$.data.authorId").value("author id required"))
                .andExpect(jsonPath("$.data.taskId").value("task id required"));
    }

    @Test
    void testDeleteByIdSuccess() throws Exception {

        doNothing().when(commentService).deleteById(1L);

        this.mockMvc.perform(delete(baseUrl + "/comment/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));

    }

    @Test
    void testDeleteByIdFail() throws Exception {

        doThrow(new EntityNotFoundException("comment not found")).when(commentService).deleteById(1L);

        this.mockMvc.perform(delete(baseUrl + "/comment/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("comment not found"));

    }
}