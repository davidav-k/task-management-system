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
/**
 * REST controller for managing users within the Task Management System.
 * Handles operations like finding, creating, updating, deleting and changing password for users
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.base-url}/user")
@Tag(name = "User", description = "User API")
public class UserController {

    private final UserService userService;

    /**
     * Handles user login and returns login information.
     * This method uses the Spring Security {@link Authentication} object to retrieve the authenticated user's details.
     *
     * @param authentication the authentication object containing user credentials
     * @return a result object containing user information and a generated JWT token
     */
    @Operation(
            summary = "User login",
            description = "Authenticates the user and returns user information along with a JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User authenticated successfully", content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class))
                    }),
                    @ApiResponse(responseCode = "401",
                            content = {@Content(schema = @Schema(implementation = Result.class), mediaType = "application/json")})
            }
    )
    @PostMapping("/login")
    public Result getLoginInfo(Authentication authentication) {
        log.debug("Authenticated user: {}", authentication.getName());
        Map<String, Object> loginInfo = userService.createLoginInfo(authentication);
        return new Result(true, StatusCode.SUCCESS, "User info and token", loginInfo);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the ID of the user to retrieve
     * @return a result object containing the user data if found
     */
    @Operation(
            summary = "Get user by ID",
            description = "Fetches a user by their unique ID and returns the user data.",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "User found successfully", content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UserRs.class))
                    }),
                    @ApiResponse(responseCode = "404",
                            description = "User not found")
            }
    )
    @GetMapping("/{id}")
    public Result findById(@PathVariable Long id) {
        UserRs rs = userService.findByIdReturnUserRs(id);
        return new Result(true, StatusCode.SUCCESS, "Found one success", rs);
    }

    /**
     * Retrieves all users in the system.
     *
     * @return a result object containing a list of all users
     */
    @Operation(
            summary = "Get all users",
            description = "Get all users. Return list a result object containing a list of all users. Available only to users with a role ADMIN",
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

    /**
     * Creates a new user in the system.
     *
     * @param rq the request object containing user details
     * @return a result object containing the created user's details
     */
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

    /**
     * Updates a user by their ID with the provided details.
     *
     * @param id the ID of the user to update
     * @param rq the request object containing updated user details
     * @return a result object containing the updated user details
     */
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


    /**
     * Deletes a user by their ID.
     *
     * @param id the ID of the user to delete
     * @return a result object indicating the success of the deletion
     */
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


    /**
     * Changes the password of a user identified by their ID.
     *
     * @param userId the ID of the user whose password is being changed
     * @param rq     the request object containing the old password, new password, and confirmation of the new password
     * @return a result object indicating the success of the password change
     */
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
