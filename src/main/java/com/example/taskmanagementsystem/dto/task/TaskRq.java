package com.example.taskmanagementsystem.dto.task;

import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;


/**
 * A request object for creating or updating a task.
 */
@Schema(description = "A request object for creating or updating a task")
public record TaskRq(
        @Schema(description = "Title of the task",
                example = "Fix bug in the login page")
        @Size(min = 3, max = 20, message = "Title must be from {min} to {max}")
        String title,

        @Schema(description = "Description of the task",
                example = "The login button is not responding to clicks")
        @Size(min = 5, max = 50, message = "Description must be from {min} to {max}")
        String description,

        @Schema(description = "Status of the task",
                example = "WAITING")
        @NotNull(message = "Status must not be null")
        Status status,

        @Schema(description = "Priority of the task",
                example = "HIGH")
        @NotNull(message = "Priority must not be null")
        Priority priority,

        @Schema(description = "ID of the author",
                example = "1")
        @Positive(message = "Author id must be > 0")
        Long authorId,

        @Schema(description = "ID of the assignee",
                example = "2")
        @Positive(message = "Assignee id must be > 0")
        Long assigneeId
) {
}