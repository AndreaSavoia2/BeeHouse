package com.prj.beehouse.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class LoginRequest {

    @NotBlank
    @Size(max = 101, min = 2)
    @Schema(description = "Registered user's username", example = "john.doe", required = true)
    private String username;

    @NotBlank
    @Schema(description = "User password", example = "password123", required = true)
    private String password;
}
