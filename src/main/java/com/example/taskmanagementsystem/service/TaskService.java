package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.dto.task.*;
import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
import com.example.taskmanagementsystem.entity.Task;
import com.example.taskmanagementsystem.repo.TaskRepository;
import com.example.taskmanagementsystem.repo.TaskSpecification;
import com.example.taskmanagementsystem.repo.TaskSpecs;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskToTaskRsConvertor taskToTaskRsConvertor;
    private final TaskRqToTaskConvertor taskRqToTaskConvertor;

    public Task findById(Long id) {
        return taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                MessageFormatter.format("Task with id {} not found", id).getMessage()));
    }

    public TaskRs findByIdReturnTaskRs(Long id) {
        Task task = findById(id);
        return taskToTaskRsConvertor.convert(task);
    }

    public Page<TaskRs> findAll(Pageable pageable) {
        Page<Task> tasksPage = taskRepository.findAll(pageable);
        return tasksPage.map(taskToTaskRsConvertor::convert);
    }

    @Transactional
    public TaskRs create(TaskRq rq) {
        Task newTask = Optional.ofNullable(taskRqToTaskConvertor.convert(rq))
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormatter.format("Conversion failed task {}", rq.title()).getMessage()));
        Task task = taskRepository.save(newTask);
        return taskToTaskRsConvertor.convert(task);
    }

    @Transactional
    public TaskRs update(Long id, TaskRq rq) {

        Task convertedTask = Optional.ofNullable(taskRqToTaskConvertor.convert(rq))
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormatter.format("Conversion failed task {}", rq.title()).getMessage()));
        Task existingTask = findById(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");

        // If the user is admin or author update all
        if (authentication.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN")) ||
                Objects.equals(existingTask.getAuthorId(), userId)) {
            existingTask.setTitle(convertedTask.getTitle());
            existingTask.setDescription(convertedTask.getDescription());
            existingTask.setPriority(convertedTask.getPriority());
            existingTask.setStatus(convertedTask.getStatus());
            existingTask.setAuthor(convertedTask.getAuthor());
            existingTask.setAssignee(convertedTask.getAssignee());
        } else { //If the user is assignee then can only update status
            if (Objects.equals(existingTask.getAssigneeId(), userId)) {
                existingTask.setStatus(convertedTask.getStatus());
            }
        }

        Task task = taskRepository.save(existingTask);

        return taskToTaskRsConvertor.convert(task);
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        taskRepository.deleteById(id);
    }

    @Transactional
    public Page<TaskRs> filterBy(TaskFilter filter) {
        Specification<Task> spec = TaskSpecification.withFilter(filter);
        PageRequest pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize());

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        return taskPage.map(taskToTaskRsConvertor::convert);
    }

    @Transactional
    public Page<TaskRs> findByCriteria(@NotNull Map<String, String> searchCriteria, Pageable pageable) throws IllegalAccessException {
        Specification<Task> spec = Specification.where(null);

        if (StringUtils.hasLength(searchCriteria.get("id"))) {
            spec = spec.and(TaskSpecs.hasId(Long.valueOf(searchCriteria.get("id"))));
        }
        if (StringUtils.hasLength(searchCriteria.get("title"))) {
            spec = spec.and(TaskSpecs.containsTitle(searchCriteria.get("title")));
        }
        if (StringUtils.hasLength(searchCriteria.get("description"))) {
            spec = spec.and(TaskSpecs.containsDescription(searchCriteria.get("description")));
        }
        if (StringUtils.hasLength(searchCriteria.get("status"))) {
            try {
                Status status = Status.valueOf(searchCriteria.get("status").toUpperCase());
                spec = spec.and(TaskSpecs.hasStatus(status));
            } catch (IllegalArgumentException ex) {
                throw new IllegalAccessException("Invalid status value:" + searchCriteria.get("status"));
            }
        }
        if (StringUtils.hasLength(searchCriteria.get("priority"))) {
            try {
                Priority priority = Priority.valueOf(searchCriteria.get("priority").toUpperCase());
                spec = spec.and(TaskSpecs.hasPriority(priority));
            } catch (IllegalArgumentException ex) {
                throw new IllegalAccessException("Invalid priority value:" + searchCriteria.get("priority"));
            }
        }
        if (StringUtils.hasLength(searchCriteria.get("authorUsername"))) {
            spec = spec.and(TaskSpecs.hasAuthorUsername(searchCriteria.get("authorUsername")));
        }
        if (StringUtils.hasLength(searchCriteria.get("assigneeUsername"))) {
            spec = spec.and(TaskSpecs.hasAssigneeUsername(searchCriteria.get("assigneeUsername")));
        }

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        return taskPage.map(taskToTaskRsConvertor::convert);
    }
}
