package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.comment.CommentRq;
import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
/**
 * REST controller for managing comments within the Task Management System.
 * Handles operations like creating, updating, and deleting comments.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/comment")
@Tag(name = "Comment", description = "Comment API")
public class CommentController {

    private final CommentService commentService;
    /**
     * Return comment for a given id.
     *
     * @param id the ID of the comment
     * @return a comment associated with the ID
     */
    @Operation(
            summary = "Get comment by id",
            description = "Return comment, author id, task id comment's with a specific ID. " +
                    "Available only to users with a roles ADMIN, USER",
            tags = {"comment", "id"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}
            ),
            @ApiResponse(
                    responseCode = "404",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}
            )
    })
    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        CommentRs rs = commentService.findByIdReturnCommentRs(id);
        return new Result(true, StatusCode.SUCCESS, "Find one success", rs);
    }

    /**
     * Adds a new comment to a task.
     *
     * @param rq request DTO comment to add
     * @return Result with the created DTO response comment
     */
    @Operation(summary = "Add a comment",
            description = "Adds a new comment to the specified task")
    @PostMapping
    public Result create(@RequestBody @Valid CommentRq rq) {
        CommentRs rs = commentService.create(rq);
        return new Result(true, StatusCode.SUCCESS, "Comment created", rs);
    }

    /**
     * Deletes a comment by its ID.
     *
     * @param id the ID of the comment to delete
     * @return object Result with operation result
     */
    @Operation(summary = "Delete a comment",
            description = "Deletes a specific comment by its ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}
            ),
            @ApiResponse(
                    responseCode = "404",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}
            )
    })
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        commentService.deleteById(id);
        return new Result(true,StatusCode.SUCCESS, "Delete success");
    }
}
