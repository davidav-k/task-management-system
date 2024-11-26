package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.task.TaskFilter;
import com.example.taskmanagementsystem.dto.task.TaskRq;
import com.example.taskmanagementsystem.dto.task.TaskRs;
import com.example.taskmanagementsystem.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
/**
 * REST controller for managing tasks within the Task Management System.
 * Handles operations like creating, updating, assigning, and deleting tasks.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/task")
public class TaskController {
    private final TaskService taskService;


    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        TaskRs rs = taskService.findByIdReturnTaskRs(id);
        return new Result(true, StatusCode.SUCCESS, "Found one", rs);
    }

    /**
     * Fetches all tasks.
     *
     * @return a Result with list of all DTO tasks
     */
    @Operation(summary = "Get all tasks", description = "Retrieves all tasks in the system")

    @GetMapping
    public Result findAll(Pageable pageable) {
        Page<TaskRs> taskRsPage = taskService.findAll(pageable);
        return new Result(true, StatusCode.SUCCESS, "Found all", taskRsPage);
    }

    /**
     * Creates a new task.
     *
     * @param rq request to create task
     * @return the Result with DTO created task
     */
    @Operation(summary = "Create a task", description = "Creates a new task with the provided details")
    @PostMapping
    public Result create(@RequestBody @Valid TaskRq rq) {
        TaskRs rs = taskService.create(rq);
        return new Result(true, StatusCode.SUCCESS, "Task created", rs);
    }

    /**
     * Updates the details of an existing task.
     *
     * @param id the ID of the task to update
     * @param rq request the updated task details
     * @return the DTO updated task
     */
    @Operation(summary = "Update a task", description = "Updates the details of an existing task")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody @Valid TaskRq rq) {
        TaskRs rs = taskService.update(id, rq);
        return new Result(true, StatusCode.SUCCESS, "Update success", rs);
    }


    /**
     * Deletes a task by its ID.
     *
     * @param id the ID of the task to delete
     */
    @Operation(summary = "Delete a task", description = "Deletes a specific task by its ID")

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        taskService.deleteById(id);
        return new Result(true, StatusCode.SUCCESS, "Delete success");
    }


    @Operation(
            summary = "Filter tasks",
            description = "Retrieve tasks based on the provided filter criteria. Supports pagination and filtering by attributes such as title, description, status, priority, author, and assignee."
    )
    @GetMapping("/filter")
    public Result findAllByFilter(@Valid @ParameterObject TaskFilter filter) {
        Page<TaskRs> taskRsPage = taskService.filterBy(filter);
        return new Result(true, StatusCode.SUCCESS, "Filtered tasks", taskRsPage);
    }

    @Operation(
            summary = "Search tasks by criteria",
            description = "Searches tasks dynamically using criteria specified as key-value pairs. Supports filtering by title, description, status, priority, and user information."
    )
    @PostMapping("/search")
    public Result findTasksByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) throws IllegalAccessException {
        Page<TaskRs> taskRsPage = taskService.findByCriteria(searchCriteria, pageable);
        return new Result(true, StatusCode.SUCCESS, "Search result", taskRsPage);
    }





}
