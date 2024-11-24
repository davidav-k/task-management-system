package com.example.taskmanagementsystem.dto.task;

import com.example.taskmanagementsystem.entity.Task;
import com.example.taskmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskRqToTaskConvertor implements Converter<TaskRq, Task> {

    private final UserService userService;

    @Override
    public Task convert(TaskRq rq) {
        return Task.builder()
                .title(rq.title())
                .description(rq.description())
                .status(rq.status())
                .priority(rq.priority())
                .author(userService.findById(rq.authorId()))
                .assignee(userService.findById(rq.assigneeId()))
                .build();
    }
}
