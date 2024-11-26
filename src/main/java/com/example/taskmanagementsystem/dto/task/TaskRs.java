package com.example.taskmanagementsystem.dto.task;



import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * A response object representing the details of a task.
 */
@Schema(description = "A response object containing task details")
public record TaskRs(
        @Schema(description = "Unique identifier of the task",
                example = "1")
        Long id,

        @Schema(description = "Title of the task",
                example = "Fix bug in the login page")
        String title,

        @Schema(description = "Description of the task",
                example = "The login button is not responding to clicks")
        String description,

        @Schema(description = "Status of the task",
                example = "WAITING")
        Status status,

        @Schema(description = "Priority of the task",
                example = "HIGH")
        Priority priority,

        @Schema(description = "ID of the author of the task",
                example = "10")
        Long authorId,

        @Schema(description = "ID of the assignee of the task",
                example = "20")
        Long assigneeId,

        @Schema(description = "Timestamp of when the task was created",
                example = "2023-11-21T12:00:00Z")
        Instant createdAt,

        @Schema(description = "List of comments associated with the task")
        List<CommentRs> commentsRs
) {
}

