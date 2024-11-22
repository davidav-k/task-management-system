package com.example.taskmanagementsystem.security;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("${api.endpoint.base-url}/user")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result getLoginInfo(Authentication authentication) {
        log.debug("Authenticated user: {}", authentication.getName());
        return new Result(true, StatusCode.SUCCESS, "User info and token",
                authService.createLoginInfo(authentication));

    }

}
