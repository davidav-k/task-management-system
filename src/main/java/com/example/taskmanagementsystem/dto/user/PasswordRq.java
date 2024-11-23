package com.example.taskmanagementsystem.dto.user;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

public record PasswordRq (
    String oldPassword,
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$",
            message = "Password must contain at least one digit, one lowercase letter, one uppercase letter, and be at least 8 characters long")
    String newPassword,

    String confirmNewPassword){}