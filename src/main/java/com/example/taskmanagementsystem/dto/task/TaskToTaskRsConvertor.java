package com.example.taskmanagementsystem.dto.task;


import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.dto.comment.CommentToCommentRsConverter;
import com.example.taskmanagementsystem.entity.Comment;
import com.example.taskmanagementsystem.entity.Task;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TaskToTaskRsConvertor implements Converter<Task, TaskRs> {

    private final CommentToCommentRsConverter commentToCommentRsConverter;

    @Override
    public TaskRs convert(@NotNull Task source) {
        return new TaskRs(
                source.getId(),
                source.getTitle(),
                source.getDescription(),
                source.getStatus(),
                source.getPriority(),
                source.getAuthorId(),
                source.getAssigneeId(),
                source.getCreatedAt(),
                setCommentsRs(source.getComments()));
    }

    private List<CommentRs> setCommentsRs(List<Comment> comments) {
        return comments.stream()
                .map(commentToCommentRsConverter::convert)
                .collect(Collectors.toList());
    }
}
