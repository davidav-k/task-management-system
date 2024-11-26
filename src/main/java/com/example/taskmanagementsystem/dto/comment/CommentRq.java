package com.example.taskmanagementsystem.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * A request object for creating a comment.
 */
@Schema(description = "A request object for creating a comment")
public record CommentRq(
        @Schema(description = "Content of the comment",
                example = "This needs to be fixed ASAP")
        @Size(min = 3, max = 30, message = "Length must be from {min} to {max}")
        String comment,

        @Schema(description = "ID of the author of the comment",
                example = "10")
        @NotNull(message = "author id required")
        @Positive(message = "author id must be > 0")
        Long authorId,

        @Schema(description = "ID of the task associated with the comment",
                example = "1")
        @NotNull(message = "task id required")
        @Positive(message = "task id must be > 0")
        Long taskId
) {
}
