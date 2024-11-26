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
import org.testcontainers.junit.jupiter.Testcontainers;

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
@Testcontainers
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

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void findById_ShouldReturnTaskRsWithCommentsRs() throws Exception {
        Instant instant = Instant.now();
        User admin = User.builder().id(1L).username("admin").password("Password123").email("admin@mail.com").roles(Set.of(RoleType.ROLE_ADMIN)).build();
        User user = User.builder().id(2L).username("user").password("Password123").email("user@mail.com").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.LOW).author(admin).assignee(user).createdAt(instant).comments(new ArrayList<>()).build();
        CommentRs commentRs1 = new CommentRs(1L, "Comment1", 1L, 1L, instant);
        CommentRs commentRs2 = new CommentRs(2L, "Comment2", 2L, 1L, instant);
        TaskRs taskRs = new TaskRs(1L, "Task", "Description task", Status.WAITING, Priority.LOW,
                1L, 1L, instant, List.of(commentRs1, commentRs2));

        given(taskService.findByIdReturnTaskRs(anyLong())).willReturn(taskRs);
        given(taskToTaskRsConvertor.convert(task)).willReturn(taskRs);

        this.mockMvc.perform(get(baseUrl + "/task/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.['flag']").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found one"))
                .andExpect(jsonPath("$.data.title").value("Task"))
                .andExpect(jsonPath("$.data.commentsRs[0].comment").value("Comment1"))
                .andExpect(jsonPath("$.data.commentsRs[1].comment").value("Comment2"));
    }

    @Test
    void findById_ShouldThrowException_WhenTaskNotFound() throws Exception {

        given(taskService.findByIdReturnTaskRs(anyLong())).willThrow(new EntityNotFoundException("Task with id 3 not found"));

        this.mockMvc.perform(get(baseUrl + "/task/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Task with id 3 not found"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void findAll_Success() throws Exception {
        Instant instant = Instant.now();
        CommentRs commentRs1 = new CommentRs(1L, "Comment1", 1L, 1L, instant);
        CommentRs commentRs2 = new CommentRs(2L, "Comment2", 2L, 1L, instant);
        TaskRs taskRs1 = new TaskRs(1L, "Task", "Description task", Status.WAITING, Priority.LOW, 1L, 1L, Instant.now(), List.of(commentRs1, commentRs2));
        TaskRs taskRs2 = new TaskRs(2L, "Task2", "Description task2", Status.WAITING, Priority.LOW, 1L, 1L, Instant.now(), List.of());
        List<TaskRs> taskRsList = new ArrayList<>(List.of(taskRs1, taskRs2));
        User admin = User.builder().id(1L).username("admin").password("Password123").email("admin@mail.com").roles(Set.of(RoleType.ROLE_ADMIN)).build();
        User user = User.builder().id(2L).username("user").password("Password123").email("user@mail.com").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task1 = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.LOW).author(admin).assignee(user).createdAt(instant).comments(new ArrayList<>()).build();
        Task task2 = new Task();
        List<Task> tasks = new ArrayList<>(List.of(task1, task2));
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<TaskRs> taskRsPage = new PageImpl<>(taskRsList, pageable, taskRsList.size());
        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs1);
        given(taskService.findAll(any(Pageable.class))).willReturn(taskRsPage);
        MultiValueMap <String, String> requestParams = new LinkedMultiValueMap<>();
        requestParams.add("page","0");
        requestParams.add("size","20");


        mockMvc.perform(get(baseUrl + "/task").accept(MediaType.APPLICATION_JSON).params(requestParams))
                .andDo(print())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Found all"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content[0].title").value("Task"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[1].comment").value("Comment2"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }

    @Test
    public void create_ShouldSaveTask() throws Exception {
        Instant instant = Instant.now();
        User admin = User.builder().id(1L).username("admin").password("Password123").email("admin@mail.com").roles(Set.of(RoleType.ROLE_ADMIN)).build();
        User user = User.builder().id(2L).username("user").password("Password123").email("user@mail.com").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.LOW).author(admin).assignee(user).createdAt(instant).comments(new ArrayList<>()).build();
        TaskRs taskRs = new TaskRs(2L, "Task", "Description task", Status.WAITING, Priority.LOW, 1L, 1L, Instant.now(), List.of());
        TaskRq rq = new TaskRq("Task", "Create Task", Status.WAITING, Priority.LOW, 1L, 1L);
        given(taskRqToTaskConvertor.convert(rq)).willReturn(task);
        given(taskService.create(rq)).willReturn(taskRs);

        this.mockMvc.perform(post(baseUrl + "/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Task created"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.title").value("Task"));
    }

    @Test
    void create_NotValidTaskDataFail() throws Exception {
        TaskRq rq = new TaskRq("", "", null, null, null, null);

        this.mockMvc.perform(post(baseUrl + "/task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("Provided arguments are not valid"))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.title").value("Title must be from 3 to 20"))
                .andExpect(jsonPath("$.data.description").value("Description must be from 5 to 50"))
                .andExpect(jsonPath("$.data.status").value("Status must not be null"))
                .andExpect(jsonPath("$.data.priority").value("Priority must not be null"));
    }

    @Test
    void update_ShouldUpdateTask() throws Exception {
        Instant instant = Instant.now();
        TaskRq rq = new TaskRq("TaskUP", "Update Task", Status.WAITING, Priority.LOW, 1L, 1L);
        Task task = Task.builder().id(1L).title("TaskUP").description("Update Task").status(Status.FINISHED).priority(Priority.HIGH).author(null).assignee(null).createdAt(instant).comments(List.of()).build();
        TaskRs taskRs = new TaskRs(1L, "TaskUP", "Update Task", Status.WAITING, Priority.LOW, 1L, 1L, instant, List.of());
        given(taskRqToTaskConvertor.convert(rq)).willReturn(task);
        given(taskService.update(anyLong(), any(TaskRq.class))).willReturn(taskRs);
        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs);

        this.mockMvc.perform(put(baseUrl + "/task/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rq))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update success"))
                .andExpect(jsonPath("$.data.title").value("TaskUP"))
                .andExpect(jsonPath("$.data.description").value("Update Task"));
    }

    @Test
    void deleteById_ShouldDeleteTask() throws Exception {
        doNothing().when(taskService).deleteById(1L);

        this.mockMvc.perform(delete(baseUrl + "/task/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete success"));
    }

    @Test
    void deleteById_ShouldThrowException_WhenTaskNotFound() throws Exception {
        doThrow(new EntityNotFoundException("task not found")).when(taskService).deleteById(1L);

        this.mockMvc.perform(delete(baseUrl + "/task/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("task not found"));
    }

    @Test
    void filter_ShouldTasksByAuthorId() throws Exception {
        Instant instant = Instant.now();
        CommentRs commentRs1 = new CommentRs(1L, "Comment1", 1L, 1L, instant);
        CommentRs commentRs2 = new CommentRs(2L, "Comment2", 2L, 1L, instant);
        TaskRs taskRs1 = new TaskRs(1L, "Task", "Description task", Status.WAITING, Priority.LOW, 1L, 1L, Instant.now(), List.of(commentRs1, commentRs2));
        TaskRs taskRs2 = new TaskRs(2L, "Task2", "Description task2", Status.WAITING, Priority.LOW, 1L, 1L, Instant.now(), List.of());
        List<TaskRs> taskRsList = new ArrayList<>(List.of(taskRs1, taskRs2));
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<TaskRs> taskRsPage = new PageImpl<>(taskRsList, pageable, taskRsList.size());
        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs1);
        given(taskService.filterBy(Mockito.any(TaskFilter.class))).willReturn(taskRsPage);

        this.mockMvc.perform(get(baseUrl + "/task/filter?pageNumber=0&pageSize=10&authorId=1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Filtered tasks"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content[0].title").value("Task"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[1].comment").value("Comment2"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }

    @Test
    void filter_ShouldTasksByAssigneeId() throws Exception {
        Instant instant = Instant.now();
        CommentRs commentRs1 = new CommentRs(1L, "Comment1", 1L, 1L, instant);
        CommentRs commentRs2 = new CommentRs(2L, "Comment2", 2L, 1L, instant);
        TaskRs taskRs1 = new TaskRs(1L, "Task", "Description task", Status.WAITING, Priority.LOW, 1L, 1L, Instant.now(), List.of(commentRs1, commentRs2));
        TaskRs taskRs2 = new TaskRs(2L, "Task2", "Description task2", Status.WAITING, Priority.LOW, 1L, 1L, Instant.now(), List.of());
        List<TaskRs> taskRsList = new ArrayList<>(List.of(taskRs1, taskRs2));
        Pageable pageable = PageRequest.of(0, 20);
        PageImpl<TaskRs> taskRsPage = new PageImpl<>(taskRsList, pageable, taskRsList.size());
        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs1);
        given(taskService.filterBy(Mockito.any(TaskFilter.class))).willReturn(taskRsPage);

        given(taskToTaskRsConvertor.convert(any(Task.class))).willReturn(taskRs1);
        given(taskService.filterBy(Mockito.any(TaskFilter.class))).willReturn(taskRsPage);

        this.mockMvc.perform(get(baseUrl + "/task/filter?pageNumber=0&pageSize=10&assigneeId=2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Filtered tasks"))
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.content[0].title").value("Task"))
                .andExpect(jsonPath("$.data.content[0].commentsRs[0].comment").value("Comment1"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(2)));
    }
}