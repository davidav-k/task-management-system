package com.example.taskmanagementsystem.dto.task;

import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;
import com.example.taskmanagementsystem.validation.TaskFilterValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TaskFilterValid
public class TaskFilter {

    private Integer pageSize;
    private Integer pageNumber;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private Long authorId;
    private Long assigneeId;
    private Instant createdAt;

}
