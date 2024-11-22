package com.example.taskmanagementsystem.dto.comment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CommentRq(

        @Size(min = 3, max = 30, message = "Length must be from {min} to {max}")
        String comment,

        @NotNull(message = "author id required")
        @Positive(message = "author id must be > 0")
        Long authorId,

        @NotNull(message = "task id required")
        @Positive(message = "task id must be > 0")
        Long taskId) {
}
