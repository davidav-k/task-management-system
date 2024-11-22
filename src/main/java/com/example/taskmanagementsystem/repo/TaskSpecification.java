package com.example.taskmanagementsystem.repo;



import com.example.taskmanagementsystem.dto.task.TaskFilter;
import com.example.taskmanagementsystem.entity.Task;
import org.springframework.data.jpa.domain.Specification;

public interface TaskSpecification {

    static Specification<Task> withFilter(TaskFilter filter) {
        return Specification.where(byAuthorId(filter.getAuthorId()))
                .or(byAssigneeId(filter.getAssigneeId()));
    }

    static Specification<Task> byAssigneeId(Long assigneeId) {
        return (root, query, criteriaBuilder) -> {
            if (assigneeId == null){
                return null;
            }
            return criteriaBuilder.equal(root.get("assignee").get("id"), assigneeId);
        };
    }


    static Specification<Task> byAuthorId(Long authorId) {
        return (root, query, criteriaBuilder) -> {
            if (authorId == null){
                return null;
            }
            return criteriaBuilder.equal(root.get("author").get("id"), authorId);
        };

    }
}
