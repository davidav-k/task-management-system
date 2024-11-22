package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.comment.CommentRq;
import com.example.taskmanagementsystem.dto.comment.CommentRqToCommentConverter;
import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.dto.comment.CommentToCommentRsConverter;
import com.example.taskmanagementsystem.entity.Comment;
import com.example.taskmanagementsystem.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/comment")
public class CommentController {

    private final CommentService commentService;
    private final CommentToCommentRsConverter commentToCommentRsConverter;
    private final CommentRqToCommentConverter commentRqToCommentConverter;
    //Todo converters to services
    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {

        return new Result(true, StatusCode.SUCCESS, "Find one success",
                commentToCommentRsConverter.convert(commentService.findById(id)));
    }

    @GetMapping
    public Result findAll() {
        List<Comment> comments = commentService.findAll();
        List<CommentRs> commentRs = comments.stream()
                .map(commentToCommentRsConverter::convert)
                .toList();

        return new Result(true, StatusCode.SUCCESS, "Find all success", commentRs);
    }

    @PostMapping
    public Result create(@RequestBody @Valid CommentRq rq) {
        Comment commentSaved = commentService.create(
                commentRqToCommentConverter.convert(rq));

        return new Result(true, StatusCode.SUCCESS, "Create success",
                commentToCommentRsConverter.convert(commentSaved));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody @Valid CommentRq rq) {

        Comment comment = commentService.update(id, commentRqToCommentConverter.convert(rq));

        return new Result(true,StatusCode.SUCCESS,"Update success",
                commentToCommentRsConverter.convert(comment));
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        commentService.deleteById(id);
        return new Result(true,StatusCode.SUCCESS, "Delete success");
    }
}
