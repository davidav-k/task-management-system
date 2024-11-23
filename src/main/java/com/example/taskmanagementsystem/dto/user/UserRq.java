package com.example.taskmanagementsystem.dto.user;

import com.example.taskmanagementsystem.entity.RoleType;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record UserRq(
        @Length(min = 3, max = 10, message = "Username must be from {min} to {max} symbols")
        String username,
        @NotBlank(message = "Email address cannot be empty")
        @Email(message = "The email address must be in the format user@example.com")
        String email,
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
                message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long")
        String password,
        @NotEmpty(message = "RoleType must not be null")
        Set<RoleType> roles) {
}
