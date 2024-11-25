package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.dto.task.*;
import com.example.taskmanagementsystem.entity.*;
import com.example.taskmanagementsystem.service.TaskService;
import com.example.taskmanagementsystem.util.DBDataInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles(value = "test")
class TaskControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    DBDataInitializer dbDataInitializer;
    @MockBean
    TaskRqToTaskConvertor taskRqToTaskConvertor;
    @MockBean
    TaskToTaskRsConvertor taskToTaskRsConvertor;
    @MockBean
    TaskService taskService;

    private final Instant createAt = Instant.now();

    private List<Task> tasks;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

//    @BeforeEach
//    void setUp() {
//        User admin = User.builder()
//                .id(1L)
//                .username("admin")
//                .password("admin")
//                .email("admin@mail.com")
//                .roles(Set.of(RoleType.ROLE_ADMIN))
//                .build();
//        User user = User.builder()
//                .id(2L)
//                .username("user")
//                .password("user")
//                .email("user@mail.com")
//                .roles(Set.of(RoleType.ROLE_USER))
//                .build();
//        Task t1 = Task.builder()
//                .id(1L)
//                .title("Task1")
//                .description("Description task 1")
//                .status(Status.WAITING)
//                .priority(Priority.LOW)
//                .author(admin)
//                .assignee(user)
//                .createdAt(createAt)
//                .comments(new ArrayList<>())
//                .build();
//        Task t2 = Task.builder()
//                .id(2L)
//                .title("Task2")
//                .description("Description task 2")
//                .status(Status.WAITING)
//                .priority(Priority.HIGH)
//                .author(user)
//                .assignee(admin)
//                .createdAt(createAt)
//                .comments(new ArrayList<>())
//                .build();
//        Comment c1 = Comment.builder().id(1L).comment("Comment1").author(admin).createAt(createAt).build();
//        Comment c2 = Comment.builder().id(2L).comment("Comment2").author(user).createAt(createAt).build();
//        Comment c3 = Comment.builder().id(3L).comment("Comment3").author(admin).createAt(createAt).build();
//        t1.addComment(c1);
//        t1.addComment(c2);
//        t2.addComment(c3);
//        tasks = List.of(t1, t2);
//    }
//
//    @AfterEach
//    void tearDown() {
//    }
//
//    @Test
//    void testFindByIdTaskWithCommentsSuccess() throws Exception {
//
//        CommentRs commentRs1 = new CommentRs(1L, "Comment1", 1L, 1L, createAt);
//        CommentRs commentRs2 = new CommentRs(2L, "Comment2", 2L, 1L, createAt);
//        TaskRs taskRs = new TaskRs(1L, "Task1", "description task1", Status.WAITING, Priority.LOW,
//                1L, 1L, Instant.now(), List.of(commentRs1, commentRs2));
//
//        given(taskService.findByIdReturnTaskRs(1L)).willReturn(tasks.get(0));
//        given(taskToTaskRsConvertor.convert(tasks.get(0))).willReturn(taskRs);
//
//        this.mockMvc.perform(get(baseUrl + "/task/1").accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.['flag']").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Found one"))
//                .andExpect(jsonPath("$.data.title").value("Task1"))
//                .andExpect(jsonPath("$.data.commentsRs[0].comment").value("Comment1"))
//                .andExpect(jsonPath("$.data.commentsRs[1].comment").value("Comment2"));
//    }
//
//    @Test
//    void testFindByIdTaskWithCommentsFail() throws Exception {
//
//        given(taskService.findByIdReturnTaskRs(3L)).willThrow(new EntityNotFoundException("Comment with id 3 not found"));
//
//        this.mockMvc.perform(get(baseUrl + "/comment/3").accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(false))
//                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
//                .andExpect(jsonPath("$.message").value("Comment with id 3 not found"))
//                .andExpect(jsonPath("$.data").doesNotExist());
//    }
//
//    @Test
//    void testFindAllSuccess() throws Exception {
//
//        CommentRs commentRs1 = new CommentRs(1L, "Comment1", 1L, 1L, createAt);
//        CommentRs commentRs2 = new CommentRs(2L, "Comment2", 2L, 1L, createAt);
//        TaskRs taskRs = new TaskRs(1L, "Task1", "description task1", Status.WAITING, Priority.LOW,
//                1L, 1L, Instant.now(), List.of(commentRs1, commentRs2));
//        Pageable pageable = PageRequest.of(0, 20);
//        PageImpl<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());
//        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs);
//        given(taskService.findAll(any(Pageable.class))).willReturn(taskPage);
//
//        MultiValueMap <String, String> requestParams = new LinkedMultiValueMap<>();
//        requestParams.add("page","0");
//        requestParams.add("size","20");
//
//
//        mockMvc.perform(get(baseUrl + "/task").accept(MediaType.APPLICATION_JSON).params(requestParams))
//                .andDo(print())
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Found all"))
//                .andExpect(jsonPath("$.data.content").exists())
//                .andExpect(jsonPath("$.data.content[0].title").value("Task1"))
//                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
//                .andExpect(jsonPath("$.data.content[0].commentsRs[1].comment").value("Comment2"))
//                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
//    }
//
//    @Test
//    public void testCreateTaskSuccess() throws Exception {
//
//        TaskRq rq = new TaskRq("Task", "Create Task", Status.WAITING, Priority.LOW, 1L, 1L);
//        given(taskRqToTaskConvertor.convert(rq)).willReturn(tasks.get(0));
//        given(taskService.create(tasks.get(0))).willReturn(tasks.get(0));
//
//        this.mockMvc.perform(post(baseUrl + "/task")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(rq))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Task created"))
//                .andExpect(jsonPath("$.data").doesNotExist());
//    }
//
//    @Test
//    void testCreateTaskFail() throws Exception {
//        TaskRq rq = new TaskRq("", "", null, null, null, null);
//
//        this.mockMvc.perform(post(baseUrl + "/task")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(rq))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(jsonPath("$.flag").value(false))
//                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
//                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
//                .andExpect(jsonPath("$.data").exists())
//                .andExpect(jsonPath("$.data.title").value("Title must be from 3 to 20"))
//                .andExpect(jsonPath("$.data.description").value("Description must be from 5 to 50"))
//                .andExpect(jsonPath("$.data.status").value("Status must not be null"))
//                .andExpect(jsonPath("$.data.priority").value("Priority must not be null"));
//    }
//
//    @Test
//    void testUpdateTaskSuccess() throws Exception {
//        TaskRq rq = new TaskRq("TaskUP", "Update Task", Status.WAITING, Priority.LOW, 1L, 1L);
//        Task task = Task.builder()
//                .id(1L)
//                .title("TaskUP")
//                .description("Update Task")
//                .status(Status.FINISHED)
//                .priority(Priority.HIGH)
//                .author(null)
//                .assignee(null)
//                .createdAt(createAt)
//                .comments(List.of())
//                .build();
//        TaskRs taskRs = new TaskRs(1L, "TaskUP", "Update Task", Status.WAITING, Priority.LOW,
//                1L, 1L, createAt, List.of());
//        given(taskRqToTaskConvertor.convert(rq)).willReturn(task);
//        given(taskService.update(anyLong(), any(Task.class))).willReturn(task);
//        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs);
//
//        this.mockMvc.perform(put(baseUrl + "/task/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(rq))
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Update success"))
//                .andExpect(jsonPath("$.data.title").value("TaskUP"))
//                .andExpect(jsonPath("$.data.description").value("Update Task"));
//    }
//
//    @Test
//    void testDeleteByIdSuccess() throws Exception {
//
//        doNothing().when(taskService).deleteById(1L);
//
//        this.mockMvc.perform(delete(baseUrl + "/task/1")
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
//        doThrow(new EntityNotFoundException("task not found")).when(taskService).deleteById(1L);
//
//        this.mockMvc.perform(delete(baseUrl + "/task/1")
//                        .accept(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(false))
//                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
//                .andExpect(jsonPath("$.message").value("task not found"))
//                .andExpect(jsonPath("$.data").isEmpty());
//
//    }
//
//    @Test
//    void testFilterTasksByAuthorSuccess() throws Exception {
//        CommentRs commentRs1 = new CommentRs(1L, "Comment1", 1L, 1L, createAt);
//        CommentRs commentRs2 = new CommentRs(2L, "Comment2", 2L, 1L, createAt);
//        TaskRs taskRs = new TaskRs(1L, "Task1", "description task1", Status.WAITING, Priority.LOW,
//                1L, 1L, Instant.now(), List.of(commentRs1, commentRs2));
//        TaskFilter filter = new TaskFilter(10,0,1L, null);
//        Pageable pageable = PageRequest.of(0, 20);
//        PageImpl<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());
//
//        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs);
//        given(taskService.filterBy(Mockito.any(TaskFilter.class))).willReturn(taskPage);
//
//
//        this.mockMvc.perform(get(baseUrl + "/task/filter?pageNumber=0&pageSize=10&authorId=1")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Filtered tasks"))
//                .andExpect(jsonPath("$.data.content").exists())
//                .andExpect(jsonPath("$.data.content[0].title").value("Task1"))
//                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
//                .andExpect(jsonPath("$.data.content[0].commentsRs[1].comment").value("Comment2"))
//                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
//    }
//
//    @Test
//    void testFilterTasksByAssigneeSuccess() throws Exception {
//
//        CommentRs commentRs1 = new CommentRs(1L, "Comment1", 1L, 2L, createAt);
//        TaskRs taskRs = new TaskRs(2L, "Task2", "description task2", Status.WAITING, Priority.LOW,
//                1L, 2L, Instant.now(), List.of(commentRs1));
//        TaskFilter filter = new TaskFilter(10,0,null, 2L);
//        Pageable pageable = PageRequest.of(0, 20);
//        PageImpl<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());
//
//        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs);
//        given(taskService.filterBy(Mockito.any(TaskFilter.class))).willReturn(taskPage);
//
//        this.mockMvc.perform(get(baseUrl + "/task/filter?pageNumber=0&pageSize=10&assigneeId=2")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.flag").value(true))
//                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
//                .andExpect(jsonPath("$.message").value("Filtered tasks"))
//                .andExpect(jsonPath("$.data.content").exists())
//                .andExpect(jsonPath("$.data.content[0].title").value("Task2"))
//                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
//                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
//    }

}