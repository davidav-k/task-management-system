package com.example.taskmanagementsystem.dto.task;



import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.entity.Priority;
import com.example.taskmanagementsystem.entity.Status;

import java.time.Instant;
import java.util.List;

public record TaskRs(Long id,
                     String title,
                     String description,
                     Status status,
                     Priority priority,
                     Long authorId,
                     Long assigneeId,
                     Instant createdAt,
                     List<CommentRs> commentsRs){

}

