package com.example.taskmanagementsystem.util;

import com.example.taskmanagementsystem.dto.user.UserRq;
import com.example.taskmanagementsystem.entity.*;
import com.example.taskmanagementsystem.service.CommentService;
import com.example.taskmanagementsystem.service.TaskService;
import com.example.taskmanagementsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Profile("test")
public class DBDataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final TaskService taskService;
    private final CommentService commentService;

    @Override
    public void run(String... args) {

        UserRq adminRq = new UserRq(
                "admin",
                "admin@mail.com",
                "Password123",
                Set.of(RoleType.ROLE_ADMIN),
                true);
        UserRq userRq = new UserRq(
                "user",
                "user@mail.com",
                "Password123",
                Set.of(RoleType.ROLE_USER),
                true);

        userService.create(adminRq);
        userService.create(userRq);
        User admin = userService.findByIdReturnUser(1L);
        User user = userService.findByIdReturnUser(2L);

        Task t1 = Task.builder()
                .title("Task1")
                .description("Description task 1")
                .status(Status.WAITING)
                .priority(Priority.LOW)
                .author(admin)
                .assignee(user)
                .comments(new ArrayList<>())
                .build();
        Task t2 = Task.builder()
                .title("Task2")
                .description("Description task 2")
                .status(Status.WAITING)
                .priority(Priority.HIGH)
                .author(user)
                .assignee(admin)
                .comments(new ArrayList<>())
                .build();
        t1 = taskService.create(t1);
        t2 = taskService.create(t2);
        Comment c1 = Comment.builder().comment("Comment1").author(admin).build();
        Comment c2 = Comment.builder().comment("Comment2").author(user).build();
        Comment c3 = Comment.builder().comment("Comment3").author(admin).build();
        Comment c4 = Comment.builder().comment("Comment4").author(admin).build();
        c1 = commentService.create(c1);
        c2 = commentService.create(c2);
        c3 = commentService.create(c3);
        c4 = commentService.create(c4);
        t1.addComment(c1);
        t1.addComment(c2);
        t2.addComment(c3);
        t2.addComment(c4);
        taskService.create(t1);
        taskService.create(t2);
    }
}
