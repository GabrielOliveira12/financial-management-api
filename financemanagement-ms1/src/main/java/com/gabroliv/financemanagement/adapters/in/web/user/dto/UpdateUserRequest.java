package com.gabroliv.financemanagement.adapters.in.web.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank(message = "Name is required.")
        @Size(max = 120, message = "Name must contain at most 120 characters.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Email must be valid.")
        @Size(max = 150, message = "Email must contain at most 150 characters.")
        String email,

        @NotBlank(message = "Username is required.")
        @Size(max = 80, message = "Username must contain at most 80 characters.")
        String username,

        @Size(min = 6, max = 255, message = "Password must contain between 6 and 255 characters.")
        String password
) {
}
