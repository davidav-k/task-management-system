package com.example.taskmanagementsystem.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * A response object representing the details of a comment.
 */
@Schema(description = "A response object containing comment details")
public record CommentRs(
        @Schema(description = "Unique identifier of the comment",
                example = "1")
        Long id,

        @Schema(description = "Content of the comment",
                example = "This needs to be fixed ASAP")
        String comment,

        @Schema(description = "ID of the author of the comment",
                example = "10")
        Long authorId,

        @Schema(description = "ID of the task associated with the comment",
                example = "1")
        Long taskId,

        @Schema(description = "Timestamp when the comment was created",
                example = "2023-11-21T12:00:00Z")
        Instant createAt
) {
}
