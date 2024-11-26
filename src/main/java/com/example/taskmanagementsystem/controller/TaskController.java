package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.task.TaskFilter;
import com.example.taskmanagementsystem.dto.task.TaskRq;
import com.example.taskmanagementsystem.dto.task.TaskRs;
import com.example.taskmanagementsystem.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    @GetMapping
    public Result findAll(Pageable pageable) {
        Page<TaskRs> taskRsPage = taskService.findAll(pageable);
        return new Result(true, StatusCode.SUCCESS, "Found all", taskRsPage);
    }

    @PostMapping
    public Result create(@RequestBody @Valid TaskRq rq) {
        TaskRs rs = taskService.create(rq);
        return new Result(true, StatusCode.SUCCESS, "Task created", rs);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody @Valid TaskRq rq) {
        TaskRs rs = taskService.update(id, rq);
        return new Result(true, StatusCode.SUCCESS, "Update success", rs);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        taskService.deleteById(id);
        return new Result(true, StatusCode.SUCCESS, "Delete success");
    }

    @GetMapping("/filter")
    public Result findAllByFilter(@Valid TaskFilter filter) {
        Page<TaskRs> taskRsPage = taskService.filterBy(filter);
        return new Result(true, StatusCode.SUCCESS, "Filtered tasks", taskRsPage);
    }

    @PostMapping("/search")
    public Result findTasksByCriteria(@RequestBody Map<String, String> searchCriteria, Pageable pageable) throws IllegalAccessException {
        Page<TaskRs> taskRsPage = taskService.findByCriteria(searchCriteria, pageable);
        return new Result(true, StatusCode.SUCCESS, "Search result", taskRsPage);
    }
}
