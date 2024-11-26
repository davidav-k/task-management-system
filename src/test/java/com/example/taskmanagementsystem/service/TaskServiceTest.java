package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.dto.task.*;
import com.example.taskmanagementsystem.entity.*;
import com.example.taskmanagementsystem.repo.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    TaskRepository taskRepository;
    @Mock
    private TaskToTaskRsConvertor taskToTaskRsConvertor;
    @Mock
    private TaskRqToTaskConvertor taskRqToTaskConvertor;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @Mock
    private Jwt jwt;
    @InjectMocks
    TaskService taskService;


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findById_ShouldReturnTask() {
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.MEDIUM).author(user).assignee(user).createdAt(instant).build();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        Task returnedTask = taskService.findById(1L);

        assertThat(returnedTask.getId()).isEqualTo(1L);
        assertThat(returnedTask.getTitle()).isEqualTo("Task");
        assertThat(returnedTask.getDescription()).isEqualTo("Description task");
        assertThat(returnedTask.getAuthorId()).isEqualTo(1L);
        assertThat(returnedTask.getAssigneeId()).isEqualTo(1L);
        assertThat(returnedTask.getCreatedAt()).isEqualTo(instant);
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenTaskDoesNotExist() {
        given(taskRepository.findById(anyLong())).willReturn(Optional.empty());

        EntityNotFoundException exception = Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> taskService.findById(1L)
        );
        assertEquals("Task with id 1 not found", exception.getMessage());
        verify(taskRepository).findById(1L);
    }

    @Test
    void findByIdReturnTaskRs_ShouldReturnTaskRs() {
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.MEDIUM).author(user).assignee(user).createdAt(instant).build();
        TaskRs taskRs = new TaskRs(1L, "Task", "Description task", Status.WAITING, Priority.MEDIUM, 1L, 1L, instant, List.of());

        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        when(taskToTaskRsConvertor.convert(task)).thenReturn(taskRs);

        TaskRs result = taskService.findByIdReturnTaskRs(1L);

        assertNotNull(result);
        verify(taskRepository).findById(1L);
        verify(taskToTaskRsConvertor).convert(task);
    }

    @Test
    void findAll_ShouldReturnPageOfTaskRs() {
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.MEDIUM).author(user).assignee(user).createdAt(instant).build();
        TaskRs taskRs = new TaskRs(1L, "Task", "Description task", Status.WAITING, Priority.MEDIUM, 1L, 1L, instant, List.of());
        Pageable pageable = PageRequest.of(0, 10);
        Task task2 = new Task();
        Page<Task> mockPage = new PageImpl<>(List.of(task, task2));
        when(taskRepository.findAll(pageable)).thenReturn(mockPage);
        when(taskToTaskRsConvertor.convert(any(Task.class))).thenReturn(taskRs);

        Page<TaskRs> result = taskService.findAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(taskRepository).findAll(pageable);
        verify(taskToTaskRsConvertor).convert(task);
    }

    @Test
    void create_ShouldSaveAndReturnTaskRs() {
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.MEDIUM).author(user).assignee(user).createdAt(instant).build();
        TaskRq taskRq = new TaskRq("Task", "Description task", Status.WAITING, Priority.MEDIUM, 1L, 1L);
        TaskRs taskRs = new TaskRs(1L, "Task", "Description task", Status.WAITING, Priority.MEDIUM, 1L, 1L, instant, List.of());
        given(taskRqToTaskConvertor.convert(taskRq)).willReturn(task);
        given(taskRepository.save(task)).willReturn(task);
        given(taskToTaskRsConvertor.convert(task)).willReturn(taskRs);

        TaskRs returnedTask = taskService.create(taskRq);

        assertThat(returnedTask.id()).isEqualTo(1L);
        assertThat(returnedTask.title()).isEqualTo("Task");
        assertThat(returnedTask.description()).isEqualTo("Description task");
        assertThat(returnedTask.createdAt()).isEqualTo(instant);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void create_ShouldThrowException_WhenConversionFails() {
        TaskRq taskRq = new TaskRq("Task", "Description task", Status.WAITING, Priority.MEDIUM, 1L, 1L);
        when(taskRqToTaskConvertor.convert(taskRq)).thenReturn(null);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class, () -> taskService.create(taskRq));

        assertTrue(exception.getMessage().contains("Conversion failed task"));
        verify(taskRqToTaskConvertor).convert(taskRq);
        verifyNoInteractions(taskRepository);
        verifyNoInteractions(taskToTaskRsConvertor);
    }

    @Test
    void update_ShouldUpdateStatusOnly_WhenUserIsNotAdminAndNotAuthor() {
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        User author = User.builder().id(2L).username("author").email("author@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        TaskRq taskRq = new TaskRq("TaskUp", "Description taskUp", Status.FINISHED, Priority.HIGH, 1L, 2L);
        Task convertedTask = Task.builder().title("TaskUp").description("Description taskUp").status(Status.FINISHED).priority(Priority.HIGH).author(user).assignee(author).build();
        Task existingTask = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.MEDIUM).author(author).assignee(user).createdAt(instant).build();
        Task updatedTask = Task.builder().id(1L).title("Task").description("Description task").status(Status.FINISHED).priority(Priority.MEDIUM).author(author).assignee(user).createdAt(instant).build();
        TaskRs taskRs = new TaskRs(1L, "Task", "Description task", Status.FINISHED, Priority.MEDIUM, 2L, 1L, instant, List.of());
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("userId")).thenReturn(1L);
        when(authentication.getAuthorities()).thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(taskRqToTaskConvertor.convert(taskRq)).thenReturn(convertedTask);
        when(taskRepository.findById(2L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(taskToTaskRsConvertor.convert(updatedTask)).thenReturn(taskRs);

        TaskRs result = taskService.update(2L, taskRq);

        assertNotNull(result);
        assertEquals("Task", result.title());
        assertEquals("Description task", result.description());
        assertEquals(Status.FINISHED, result.status());
        assertEquals(Priority.MEDIUM, result.priority());
        assertEquals(2L, result.authorId());
        assertEquals(1L, result.assigneeId());
        verify(taskRepository).save(existingTask);
        verify(taskRqToTaskConvertor).convert(taskRq);
        verify(taskToTaskRsConvertor).convert(updatedTask);
    }

    @Test
    void update_ShouldUpdateAllFields_WhenUserIsAdmin() {
        Instant instant = Instant.now();
        User admin = User.builder().id(1L).username("admin").email("admin@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_ADMIN)).build();
        User user = User.builder().id(2L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        TaskRq taskRq = new TaskRq("TaskUp", "Description taskUp", Status.FINISHED, Priority.HIGH, 1L, 2L);
        Task convertedTask = Task.builder().id(1L).title("TaskUp").description("Description taskUp").status(Status.FINISHED).priority(Priority.HIGH).author(admin).assignee(user).createdAt(instant).build();
        Task existingTask = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.MEDIUM).author(admin).assignee(admin).createdAt(instant).build();
        Task updatedTask = Task.builder().id(1L).title("TaskUp").description("Description taskUp").status(Status.FINISHED).priority(Priority.HIGH).author(admin).assignee(admin).createdAt(instant).build();
        TaskRs taskRs = new TaskRs(1L, "TaskUp", "Description taskUp", Status.FINISHED, Priority.HIGH, 1L, 1L, instant, List.of());
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaim("userId")).thenReturn(1L);
        when(authentication.getAuthorities()).thenReturn((Collection) Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        when(taskRqToTaskConvertor.convert(taskRq)).thenReturn(convertedTask);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(existingTask)).thenReturn(updatedTask);
        when(taskToTaskRsConvertor.convert(updatedTask)).thenReturn(taskRs);

        TaskRs result = taskService.update(1L, taskRq);

        assertNotNull(result);
        assertEquals("TaskUp", result.title());
        assertEquals("Description taskUp", result.description());
        assertEquals(Status.FINISHED, result.status());
        assertEquals(Priority.HIGH, result.priority());
        verify(taskRepository).save(existingTask);
        verify(taskRqToTaskConvertor).convert(taskRq);
        verify(taskToTaskRsConvertor).convert(updatedTask);
    }

    @Test
    void update_ShouldThrowException_WhenConversionFails() {
        TaskRq taskRq = new TaskRq("Task", "Description task", Status.FINISHED, Priority.MEDIUM, 1L, 1L);
        when(taskRqToTaskConvertor.convert(taskRq)).thenReturn(null);

        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,() -> taskService.update(1L, taskRq));

        assertTrue(exception.getMessage().contains("Conversion failed task"));
        verify(taskRqToTaskConvertor).convert(taskRq);
        verifyNoInteractions(taskRepository, taskToTaskRsConvertor);
    }

    @Test
    void deleteById_ShouldDeleteTask() {
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Task").description("Description task").status(Status.WAITING).priority(Priority.MEDIUM).author(user).assignee(user).createdAt(instant).build();
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        doNothing().when(taskRepository).deleteById(1L);

        taskService.deleteById(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteById_ShouldThrowException_WhenTaskDoesNotExist() {
        given(taskRepository.findById(2L)).willReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.deleteById(2L));

        verify(taskRepository, times(1)).findById(2L);
    }

    @Test
    void filterBy_withValidFilter_shouldReturnPageOfTaskRs() {
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Test Title").description("Test description").status(Status.WAITING).priority(Priority.MEDIUM).author(user).assignee(user).createdAt(instant).build();
        TaskFilter filter = new TaskFilter(10, 0, "Test Title", "Test description", Status.WAITING, Priority.MEDIUM, 1L, 1L, instant);
        TaskRs taskRs = new TaskRs(1L, "Test Title", "Test description", Status.WAITING, Priority.MEDIUM, 1L, 1L, instant, List.of());
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(taskPage);
        when(taskToTaskRsConvertor.convert(task)).thenReturn(taskRs);

        Page<TaskRs> result = taskService.filterBy(filter);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Title", result.getContent().get(0).title());
        assertEquals("Test description", result.getContent().get(0).description());
        assertEquals(Status.WAITING, result.getContent().get(0).status());
        assertEquals(Priority.MEDIUM, result.getContent().get(0).priority());
        verify(taskRepository).findAll(any(Specification.class), any(PageRequest.class));
        verify(taskToTaskRsConvertor).convert(task);
    }

    @Test
    void filterBy_withEmptyResult_shouldReturnEmptyPage() {

        TaskFilter filter = new TaskFilter();
        filter.setPageNumber(0);
        filter.setPageSize(10);

        Page<Task> emptyPage = Page.empty();
        when(taskRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(emptyPage);

        Page<TaskRs> result = taskService.filterBy(filter);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findAll(any(Specification.class), any(PageRequest.class));
        verifyNoInteractions(taskToTaskRsConvertor);
    }

    @Test
    void filterBy_withNullFilter_shouldThrowException() {
        TaskFilter filter = null;

        assertThrows(IllegalArgumentException.class, () -> taskService.filterBy(filter));

        verifyNoInteractions(taskRepository, taskToTaskRsConvertor);
    }

    @Test
    void findByCriteria_withValidCriteria_shouldReturnPageOfTaskRs() throws IllegalAccessException {

        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("title", "Test Title");
        searchCriteria.put("priority", "HIGH");
        Pageable pageable = PageRequest.of(0, 10);
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Test Title").description("Test description").status(Status.WAITING).priority(Priority.MEDIUM).author(user).assignee(user).createdAt(instant).build();
        TaskRs taskRs = new TaskRs(1L, "Test Title", "Test description", Status.WAITING, Priority.MEDIUM, 1L, 1L, instant, List.of());
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(taskPage);
        when(taskToTaskRsConvertor.convert(task)).thenReturn(taskRs);

        Page<TaskRs> result = taskService.findByCriteria(searchCriteria, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Title", result.getContent().get(0).title());
        assertEquals("Test description", result.getContent().get(0).description());
        assertEquals(Status.WAITING, result.getContent().get(0).status());
        assertEquals(Priority.MEDIUM, result.getContent().get(0).priority());
        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
        verify(taskToTaskRsConvertor).convert(task);
    }

    @Test
    void findByCriteria_withInvalidStatus_shouldThrowException() {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("status", "INVALID_STATUS");
        Pageable pageable = PageRequest.of(0, 10);

        IllegalAccessException exception = assertThrows(IllegalAccessException.class,
                () -> taskService.findByCriteria(searchCriteria, pageable));

        assertEquals("Invalid status value:INVALID_STATUS", exception.getMessage());
        verifyNoInteractions(taskRepository, taskToTaskRsConvertor);
    }

    @Test
    void findByCriteria_withInvalidPriority_shouldThrowException() {
        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("priority", "INVALID_PRIORITY");
        Pageable pageable = PageRequest.of(0, 10);

        IllegalAccessException exception = assertThrows(IllegalAccessException.class,
                () -> taskService.findByCriteria(searchCriteria, pageable));

        assertEquals("Invalid priority value:INVALID_PRIORITY", exception.getMessage());
        verifyNoInteractions(taskRepository, taskToTaskRsConvertor);
    }

    @Test
    void findByCriteria_withEmptyCriteria_shouldReturnEmptyPage() throws IllegalAccessException {
        Map<String, String> searchCriteria = new HashMap<>();
        Pageable pageable = PageRequest.of(0, 10);

        Page<Task> emptyPage = Page.empty();
        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        Page<TaskRs> result = taskService.findByCriteria(searchCriteria, pageable);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
        verifyNoInteractions(taskToTaskRsConvertor);
    }

    @Test
    void findByCriteria_withPartialCriteria_shouldReturnPageOfTaskRs() throws IllegalAccessException {

        Map<String, String> searchCriteria = new HashMap<>();
        searchCriteria.put("authorUsername", "author");
        Pageable pageable = PageRequest.of(0, 10);
        Instant instant = Instant.now();
        User user = User.builder().id(1L).username("user").email("user@mail.com").password("Password123").roles(Set.of(RoleType.ROLE_USER)).build();
        Task task = Task.builder().id(1L).title("Test Title").description("Test description").status(Status.WAITING).priority(Priority.MEDIUM).author(user).assignee(user).createdAt(instant).build();
        TaskRs taskRs = new TaskRs(1L, "Test Title", "Test description", Status.WAITING, Priority.MEDIUM, 1L, 1L, instant, List.of());
        Page<Task> taskPage = new PageImpl<>(List.of(task));
        when(taskRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(taskPage);
        when(taskToTaskRsConvertor.convert(task)).thenReturn(taskRs);

        Page<TaskRs> result = taskService.findByCriteria(searchCriteria, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(taskRepository).findAll(any(Specification.class), eq(pageable));
        verify(taskToTaskRsConvertor).convert(task);
    }
}