package com.example.taskmanagementsystem.dto.user;

import com.example.taskmanagementsystem.entity.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

/**
 * A request object for creating or updating a user.
 */
@Schema(description = "A request object for creating or updating a user")
public record UserRq(
        @Schema(description = "Username of the user",
                example = "john_doe")
        @Length(min = 3, max = 10, message = "Username must be from {min} to {max} symbols")
        String username,

        @Schema(description = "Email address of the user",
                example = "john.doe@example.com")
        @NotBlank(message = "Email address cannot be empty")
        @Email(message = "The email address must be in the format user@example.com")
        String email,

        @Schema(description = "Password for the user",
                example = "SecureP@ssword1")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
                message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long")
        String password,

        @Schema(description = "Set of roles assigned to the user",
                example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]")
        @NotEmpty(message = "RoleType must not be null or empty")
        Set<RoleType> roles,

        @Schema(description = "Indicates if the user is enabled",
                example = "true")
        boolean enabled
) {
}