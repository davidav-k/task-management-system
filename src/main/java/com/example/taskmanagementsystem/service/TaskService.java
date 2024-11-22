package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.dto.task.TaskFilter;
import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
import com.example.taskmanagementsystem.entity.Task;
import com.example.taskmanagementsystem.repo.TaskRepository;
import com.example.taskmanagementsystem.repo.TaskSpecification;
import com.example.taskmanagementsystem.repo.TaskSpecs;
import com.example.taskmanagementsystem.util.AppHelperUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;


    public Task findById(Long id) {

        return taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(
                MessageFormatter.format("Task with id {} not found", id).getMessage()));
    }

    public List<Task> findAll() {

        return taskRepository.findAll();
    }

    @Transactional
    public Task create(Task task) {

        return taskRepository.save(task);
    }

    public Task update(Long id, Task update) {
        return taskRepository.findById(id)
                .map(existedComment -> {
                    AppHelperUtils.copyNonNullProperties(update, existedComment);
                    return taskRepository.save(existedComment);
                })
                .orElseThrow(() -> new EntityNotFoundException("task not found"));
    }

    @Transactional
    public void deleteById(Long id) {
        taskRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("task not found"));
        taskRepository.deleteById(id);
    }

    public Page<Task> filterBy(TaskFilter filter) {
        Specification<Task> spec = TaskSpecification.withFilter(filter);
        PageRequest pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize());
        return taskRepository.findAll(spec, pageable);
    }

    public Page<Task> findAll(Pageable pageable) {

        return taskRepository.findAll(pageable);
    }

    public Page<Task> findByCriteria(Map<String, String> searchCriteria, Pageable pageable) throws IllegalAccessException {
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
            }catch (IllegalArgumentException ex){
                throw new IllegalAccessException("Invalid status value:" + searchCriteria.get("status"));
            }

        }

        if (StringUtils.hasLength(searchCriteria.get("priority"))) {
            try {
                Priority priority = Priority.valueOf(searchCriteria.get("priority").toUpperCase());
                spec = spec.and(TaskSpecs.hasPriority(priority));
            }catch (IllegalArgumentException ex){
                throw new IllegalAccessException("Invalid priority value:" + searchCriteria.get("priority"));
            }

        }

        if (StringUtils.hasLength(searchCriteria.get("authorUsername"))) {
            spec = spec.and(TaskSpecs.hasAuthorUsername(searchCriteria.get("authorUsername")));
        }

        if (StringUtils.hasLength(searchCriteria.get("assigneeUsername"))) {
            spec = spec.and(TaskSpecs.hasAssigneeUsername(searchCriteria.get("assigneeUsername")));
        }

        return taskRepository.findAll(spec, pageable);
    }
}

