package com.example.taskmanagementsystem.dto.task;

import com.example.taskmanagementsystem.validation.TaskFilterValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TaskFilterValid
public class TaskFilter {

    private Integer pageSize;
    private Integer pageNumber;
    private Long authorId;
    private Long assigneeId;

}
