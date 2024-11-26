package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.comment.CommentRq;
import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/comment")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        CommentRs rs = commentService.findByIdReturnCommentRs(id);
        return new Result(true, StatusCode.SUCCESS, "Find one success", rs);
    }

    @PostMapping
    public Result create(@RequestBody @Valid CommentRq rq) {
        CommentRs rs = commentService.create(rq);
        return new Result(true, StatusCode.SUCCESS, "Comment created", rs);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        commentService.deleteById(id);
        return new Result(true,StatusCode.SUCCESS, "Delete success");
    }
}
