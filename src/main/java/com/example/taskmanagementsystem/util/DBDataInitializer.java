package com.example.taskmanagementsystem.util;

import com.example.taskmanagementsystem.dto.comment.CommentRq;
import com.example.taskmanagementsystem.dto.comment.CommentRs;
import com.example.taskmanagementsystem.dto.task.TaskRq;
import com.example.taskmanagementsystem.dto.task.TaskRs;
import com.example.taskmanagementsystem.dto.user.UserRq;
import com.example.taskmanagementsystem.dto.user.UserRs;
import com.example.taskmanagementsystem.entity.*;
import com.example.taskmanagementsystem.repo.TaskRepository;
import com.example.taskmanagementsystem.service.CommentService;
import com.example.taskmanagementsystem.service.TaskService;
import com.example.taskmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Profile("test")
public class DBDataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final CommentService commentService;

    @Override
    @Transactional
    public void run(String... args) {

        UserRq adminRq = new UserRq("admin", "admin@mail.com", "Password123", Set.of(RoleType.ROLE_ADMIN), true);
        UserRq user1Rq = new UserRq("user1", "user1@mail.com", "Password123", Set.of(RoleType.ROLE_USER), true);
        UserRq user2Rq = new UserRq("user2", "user2@mail.com", "Password123", Set.of(RoleType.ROLE_USER), true);

        UserRs adminRs = userService.create(adminRq);
        UserRs user1Rs = userService.create(user1Rq);
        UserRs user2Rs = userService.create(user2Rq);

        User admin = userService.findById(adminRs.id());
        User user1 = userService.findById(user1Rs.id());
        User user2 = userService.findById(user2Rs.id());

        TaskRq taskRq1 = new TaskRq("Task1", "Description task 1", Status.WAITING, Priority.LOW, admin.getId(), user1.getId());
        TaskRq taskRq2 = new TaskRq("Task2", "Description task 2", Status.WAITING, Priority.LOW, user1.getId(), user2.getId());

        TaskRs task1Rs = taskService.create(taskRq1);
        TaskRs task2Rs = taskService.create(taskRq2);
        Task task1 = taskService.findById(task1Rs.id());
        Task task2 = taskService.findById(task2Rs.id());

        CommentRq comment1Rq = new CommentRq("Comment1", admin.getId(), task1.getId());
        CommentRq comment2Rq = new CommentRq("Comment2", user1.getId(), task1.getId());
        CommentRq comment3Rq = new CommentRq("Comment3", user1.getId(), task2.getId());
        CommentRq comment4Rq = new CommentRq("Comment4", user2.getId(), task2.getId());

        CommentRs comment1Rs = commentService.create(comment1Rq);
        CommentRs comment2Rs = commentService.create(comment2Rq);
        CommentRs comment3Rs = commentService.create(comment3Rq);
        CommentRs comment4Rs = commentService.create(comment4Rq);

        Comment comment1 = commentService.findById(comment1Rs.id());
        Comment comment2 = commentService.findById(comment2Rs.id());
        Comment comment3 = commentService.findById(comment3Rs.id());
        Comment comment4 = commentService.findById(comment4Rs.id());

        task1.addComment(comment1);
        task1.addComment(comment2);
        task2.addComment(comment3);
        task2.addComment(comment4);

        taskRepository.save(task1);
        taskRepository.save(task2);
    }
}
