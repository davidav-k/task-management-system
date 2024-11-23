package com.example.taskmanagementsystem.dto.comment;

import com.example.taskmanagementsystem.entity.Comment;
import com.example.taskmanagementsystem.service.TaskService;
import com.example.taskmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentRqToCommentConverter implements Converter<CommentRq, Comment> {

    private final TaskService taskService;
    private final UserService userService;

    @Override
    public Comment convert(CommentRq rq) {

        return Comment.builder()
                .comment(rq.comment())
                .author(userService.findByIdReturnUser(rq.authorId()))
                .task(taskService.findById(rq.taskId()))
                .build();
    }
}
