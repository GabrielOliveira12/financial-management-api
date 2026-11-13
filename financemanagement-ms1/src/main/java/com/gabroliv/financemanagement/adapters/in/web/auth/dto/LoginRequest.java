package com.gabroliv.financemanagement.adapters.in.web.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username or email is required.")
        @Schema(description = "Username or email of the user.", example = "ana.silva")
        String username,

        @NotBlank(message = "Password is required.")
        @Schema(description = "User password.", example = "user1123")
        String password
) { }
