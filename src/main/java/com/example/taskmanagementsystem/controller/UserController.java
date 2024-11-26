package com.example.taskmanagementsystem.controller;

import com.example.taskmanagementsystem.dto.Result;
import com.example.taskmanagementsystem.dto.StatusCode;
import com.example.taskmanagementsystem.dto.user.PasswordRq;
import com.example.taskmanagementsystem.dto.user.UserRq;
import com.example.taskmanagementsystem.dto.user.UserRs;
import com.example.taskmanagementsystem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User", description = "User API")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result getLoginInfo(Authentication authentication) {
        log.debug("Authenticated user: {}", authentication.getName());
        Map<String, Object> loginInfo = userService.createLoginInfo(authentication);
        return new Result(true, StatusCode.SUCCESS, "User info and token", loginInfo);
    }

    @Operation(
            summary = "Get user by id",
            description = "Return firstName, secondName, countNewses, countComments user's with a specific ID. " +
                    "Available only to users with a role ADMIN",
            tags = {"user", "id"}
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
        UserRs rs = userService.findByIdReturnUserRs(id);
        return new Result(true, StatusCode.SUCCESS, "Found one success", rs);
    }

    @Operation(
            summary = "Get all users",
            description = "Get all users. Return list of DTO users. Available only to users with a role ADMIN",
            tags = {"user"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}
            ),
            @ApiResponse(
                    responseCode = "404",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}
            )
    })
    @GetMapping
    public Result findAll() {
        List<UserRs> rs = userService.findAll();
        return new Result(true,StatusCode.SUCCESS, "Found all", rs);
    }

    @Operation(
            summary = "Create new user",
            description = "Return Result with new DTO user. Available only to users with a role ADMIN",
            tags = {"user"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}
            ),
            @ApiResponse(
                    responseCode = "404",
                    content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")}
            )
    })
    @PostMapping
    Result create(@Valid @RequestBody UserRq rq){
        UserRs rs = userService.create(rq);
        return new Result(true, StatusCode.SUCCESS, "User created successfully", rs);
    }

    @Operation(
            summary = "Edit user",
            description = "Return edited DTO user. Available only to users with a role ADMIN",
            tags = {"user", "id"}
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

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody @Valid UserRq rq) {
        UserRs rs = userService.update(id, rq);
        return new Result(true,StatusCode.SUCCESS,"Update success", rs);
    }

    @Operation(
            summary = "Delete user",
            description = "Delete user with a specific ID. " +
                    "Available only to users with a role ADMIN",
            tags = {"user", "id"}
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
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id) {
        userService.deleteById(id);
        return new Result(true,StatusCode.SUCCESS, "Delete success");
    }

    @Operation(
            summary = "Change password user",
            description = "Change password with a specific ID. " +
                    "Available only to users with a role ADMIN",
            tags = {"user", "id"}
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
    @PatchMapping("/{userId}/password")
    public Result changePassword(@PathVariable Long userId, @RequestBody @Valid PasswordRq rq){
        userService.changePassword(userId, rq);
        return new Result(true, StatusCode.SUCCESS, "Change password success");
    }
}
