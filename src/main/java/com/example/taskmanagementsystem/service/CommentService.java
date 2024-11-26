package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.dto.comment.CommentRq;
import com.example.taskmanagementsystem.dto.comment.CommentRqToCommentConverter;
import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.dto.comment.CommentToCommentRsConverter;
import com.example.taskmanagementsystem.entity.Comment;
import com.example.taskmanagementsystem.repo.CommentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentToCommentRsConverter commentToCommentRsConverter;
    private final CommentRqToCommentConverter commentRqToCommentConverter;

    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        MessageFormatter.format("Comment with id {} not found", id).getMessage()));
    }

    public CommentRs findByIdReturnCommentRs(Long id) {
        return commentToCommentRsConverter.convert(findById(id));
    }

    @Transactional
    public CommentRs create(CommentRq rq) {
        Comment newComment = Optional.ofNullable(commentRqToCommentConverter.convert(rq))
                .orElseThrow(() -> new IllegalArgumentException(
                        MessageFormatter.format("Conversion failed comment {}", rq.comment()).getMessage()));
        Comment comment = commentRepository.save(newComment);
        return commentToCommentRsConverter.convert(comment);
    }

    @Transactional
    public void deleteById(Long id) {
        findById(id);
        commentRepository.deleteById(id);
    }
}
