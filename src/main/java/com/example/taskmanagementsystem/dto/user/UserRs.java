package com.example.taskmanagementsystem.dto.user;



import com.example.taskmanagementsystem.entity.RoleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

/**
 * A response object representing user details.
 */
@Schema(description = "A response object containing user details")
public record UserRs(
        @Schema(description = "Unique identifier of the user", example = "1")
        Long id,

        @Schema(description = "Username of the user", example = "user")
        String username,

        @Schema(description = "Email address of the user", example = "user@mail.com")
        String email,

        @Schema(description = "Roles assigned to the user", example = "[\"ROLE_ADMIN\", \"ROLE_USER\"]")
        Set<RoleType> roles
) {
}
