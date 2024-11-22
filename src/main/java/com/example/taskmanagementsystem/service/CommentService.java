package com.example.taskmanagementsystem.service;

import com.example.taskmanagementsystem.entity.Comment;
import com.example.taskmanagementsystem.repo.CommentRepository;
import com.example.taskmanagementsystem.util.AppHelperUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Service
@Transactional
@ResponseBody
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public Comment findById(Long id) {
        return commentRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(
                        MessageFormatter.format("Comment with id {} not found", id).getMessage()));
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public Comment create(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment update(Long id, Comment update) {
        return commentRepository.findById(id)
                .map(existedComment -> {
                    AppHelperUtils.copyNonNullProperties(update, existedComment);
                    return commentRepository.save(existedComment);
                })
                .orElseThrow(() -> new EntityNotFoundException("comment not found"));
    }

    public void deleteById(Long id) {
        commentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("comment not found"));
        commentRepository.deleteById(id);
    }
}
