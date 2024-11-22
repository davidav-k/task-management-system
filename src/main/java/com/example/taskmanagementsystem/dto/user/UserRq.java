package com.example.taskmanagementsystem.dto.user;

import com.example.taskmanagementsystem.entity.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record UserRq(
        @Length(min = 3, max = 10, message = "Username must be from {min} to {max} symbols")
        String username,
        @NotBlank(message = "Email address cannot be empty")
        @Email(message = "The email address must be in the format user@example.com")
        String email,
        @Size(min = 4, max = 255, message = "The password length must be from 4 no more than 255 characters.")
        String password,
        @NotEmpty(message = "RoleType must not be null")
        Set<RoleType> roles) {
}
