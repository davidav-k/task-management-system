package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.user.PasswordRq;
import com.example.taskmanagementsystem.dto.user.UserRq;
import com.example.taskmanagementsystem.dto.user.UserRs;
import com.example.taskmanagementsystem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result getLoginInfo(Authentication authentication) {
        log.debug("Authenticated user: {}", authentication.getName());
        Map<String, Object> loginInfo = userService.createLoginInfo(authentication);
        return new Result(true, StatusCode.SUCCESS, "User info and token", loginInfo);
    }

    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        UserRs rs = userService.findByIdReturnUserRs(id);
        return new Result(true, StatusCode.SUCCESS, "Found one success", rs);
    }

    //TODO return page
    @GetMapping
    public Result findAll() {
        List<UserRs> rs = userService.findAll();
        return new Result(true,StatusCode.SUCCESS, "Found all", rs);
    }

    @PostMapping
    Result create(@Valid @RequestBody UserRq rq){
        UserRs rs = userService.create(rq);
        return new Result(true, StatusCode.SUCCESS, "User created successfully", rs);
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody @Valid UserRq rq) {
        UserRs rs = userService.update(id, rq);
        return new Result(true,StatusCode.SUCCESS,"Update success", rs);
    }

    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return new Result(true,StatusCode.SUCCESS, "Delete success");
    }

    @PatchMapping("/{userId}/password")
    public Result changePassword(@PathVariable Long userId, @RequestBody @Valid PasswordRq rq){
        userService.changePassword(userId, rq);
        return new Result(true, StatusCode.SUCCESS, "Change password success");
    }
}
