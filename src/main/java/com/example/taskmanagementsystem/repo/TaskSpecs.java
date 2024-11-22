package com.example.taskmanagementsystem.repo;



import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
import com.example.taskmanagementsystem.entity.Task;
import org.springframework.data.jpa.domain.Specification;

public class TaskSpecs {

    public static Specification<Task> hasId(Long providedId){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"),providedId);
        }

    public static Specification<Task> containsTitle(String providedTitle){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + providedTitle.toLowerCase() + "%");
    }

    public static Specification<Task> containsDescription(String providedDescription){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + providedDescription.toLowerCase() + "%");
    }

    public static Specification<Task> hasStatus(Status providedStatus){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), providedStatus);
    }

    public static Specification<Task> hasPriority(Priority providedPriority){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("priority"), providedPriority);
    }

    public static Specification<Task> hasAuthorUsername(String providedAuthorUsername){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("author").get("username")), providedAuthorUsername);
    }

    public static Specification<Task> hasAssigneeUsername(String providedAssigneeUsername){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(criteriaBuilder.lower(root.get("assignee").get("username")), providedAssigneeUsername);
    }

            //TODO createdAt;
}

