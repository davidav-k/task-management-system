package com.example.taskmanagementsystem.repo;


import com.example.taskmanagementsystem.dto.task.TaskFilter;
import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
import com.example.taskmanagementsystem.entity.Task;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public interface TaskSpecification {

    static @NotNull Specification<Task> withFilter(@NotNull TaskFilter filter) {
        return Specification.where(byTitle(filter.getTitle())
                        .and(byDescription(filter.getDescription()))
                        .and(byStatus(filter.getStatus()))
                        .and(byPriority(filter.getPriority()))
                        .and(byAuthorId(filter.getAuthorId())))
                .and(byAssigneeId(filter.getAssigneeId()))
                .and(byCreateAtBefore(filter.getCreatedAt()));
    }

     static @NotNull Specification<Task> byPriority(Priority priority) {
         return (root, query, criteriaBuilder) -> {
             if (priority == null) {
                 return null;
             }
             return criteriaBuilder.equal(root.get("priority"), priority);
         };
    }

    static @NotNull Specification<Task> byStatus(Status status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    static @NotNull Specification<Task> byCreateAtBefore(Instant createdAt) {

        return (root, query, criteriaBuilder) -> {
            if (createdAt == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdAt);
        };
    }

    static @NotNull Specification<Task> byTitle(String title) {
        return (root, query, cb) -> {
            if (title == null){
                return null;
            }
            return cb.equal(root.get("title"), title);
        };
    }

    static @NotNull Specification<Task> byDescription(String description) {
        return (root, query, criteriaBuilder) -> {
            if (description == null){
                return null;
            }
            return criteriaBuilder.equal(root.get("description"), description);
        };
    }

    static @NotNull Specification<Task> byAssigneeId(Long assigneeId) {
        return (root, query, criteriaBuilder) -> {
            if (assigneeId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId);
        };
    }

    static @NotNull Specification<Task> byAuthorId(Long authorId) {
        return (root, query, criteriaBuilder) -> {
            if (authorId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("author").get("id"), authorId);
        };
    }
}
