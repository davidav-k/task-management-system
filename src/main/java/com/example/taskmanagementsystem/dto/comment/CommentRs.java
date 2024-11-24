package com.example.taskmanagementsystem.dto.comment;

import java.time.Instant;

public record CommentRs(Long id,
                        String comment,
                        Long authorId,
                        Long taskId,
                        Instant createAt) {
}
