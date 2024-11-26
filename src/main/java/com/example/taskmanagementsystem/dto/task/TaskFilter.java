package com.example.taskmanagementsystem.dto.task;

import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
import com.example.taskmanagementsystem.validation.TaskFilterValid;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TaskFilterValid
@Schema(description = "Filter for tasks")
public class TaskFilter {

    @Schema(description = "Number of items per page", example = "10")
    private Integer pageSize;

    @Schema(description = "Page number to retrieve", example = "0")
    private Integer pageNumber;

    @Schema(description = "Task title to filter by", example = "Fix bug")
    private String title;

    @Schema(description = "Task description to filter by", example = "Critical issue in production")
    private String description;

    @Schema(description = "Task status", example = "RUNNING")
    private Status status;

    @Schema(description = "Task priority", example = "HIGH")
    private Priority priority;

    @Schema(description = "ID of the task author", example = "1")
    private Long authorId;

    @Schema(description = "ID of the task assignee", example = "2")
    private Long assigneeId;

    @Schema(description = "Filter tasks created after this timestamp", example = "2023-01-01T00:00:00Z")
    private Instant createdAt;

}
