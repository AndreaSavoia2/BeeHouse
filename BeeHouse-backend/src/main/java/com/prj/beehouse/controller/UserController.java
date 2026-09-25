package com.prj.beehouse.controller;

import com.prj.beehouse.payload.ResponseApi;
import com.prj.beehouse.payload.request.LoginRequest;
import com.prj.beehouse.payload.request.RegistrationRequest;
import com.prj.beehouse.security.CustomUserDetails;
import com.prj.beehouse.service.UserService;
import com.prj.beehouse.util.BuildPageableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api")
@Tag(name = "User", description = "User management")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register a new user",
            description = "Allows the administrator to register a new user. A unique username is generated and a temporary password is sent by email.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request fields", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @PostMapping("administrator/users/registration")
    public ResponseEntity<ResponseApi<String>> userRegistration(@Valid @RequestBody RegistrationRequest request) {
        return ResponseApi.buildResponse(HttpStatus.CREATED, userService.userRegistration(request));
    }

    @Operation(
            summary = "User login",
            description = "Allows the user to log in with username and password. Returns a JWT token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login completed successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request fields", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PostMapping("auth/users/login")
    public ResponseEntity<ResponseApi<String>> userLogin(@Valid @RequestBody LoginRequest request) {
        return ResponseApi.buildResponse(HttpStatus.OK, userService.authenticateUser(request));
    }

    @Operation(
            summary = "Send a username reminder",
            description = "Sends the user an email containing their username.",
            parameters = @Parameter(
                    name = "email",
                    description = "Email of the user to remind",
                    required = true,
                    example = "user@example.com"
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reminder sent successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid email parameter", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("auth/users/remind")
    public ResponseEntity<ResponseApi<String>> userRemind(@RequestParam @Email @NotBlank String email) {
        return ResponseApi.buildResponse(HttpStatus.OK, userService.remindUsername(email));
    }

    @Operation(
            summary = "Change the user's password",
            description = "Changes the authenticated user's password after verifying the old password.",
            parameters = {
                    @Parameter(
                            name = "oldPassword",
                            description = "User's old password",
                            required = true,
                            example = "OldPassword123"
                    ),
                    @Parameter(
                            name = "newPassword",
                            description = "User's new password",
                            required = true,
                            example = "NewPassword456"
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Invalid credentials or unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PatchMapping("shared/users/change/password")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','USER')")
    public ResponseEntity<ResponseApi<String>> changePassword(
            @RequestParam @NotBlank String oldPassword,
            @RequestParam @NotBlank @Size(min = 8) @Pattern(regexp = "^(?=.*\\p{Lu})(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$") String newPassword,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseApi.buildResponse(HttpStatus.OK, userService.changePassword(oldPassword, newPassword, userDetails));
    }

    @Operation(
            summary = "Change the authenticated user's username",
            description = "Updates the authenticated user's username, verifying that the new username does not already exist.",
            parameters = @Parameter(
                    name = "requestNewUsername",
                    description = "User's new username",
                    required = true,
                    example = "new.username"
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Username changed successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid username parameter", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Username already exists or operation is unauthorized", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PatchMapping("shared/users/change/username")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','USER')")
    public ResponseEntity<ResponseApi<String>> changeUsername(
            @RequestParam @NotBlank @Size(max = 101, min = 1) String requestNewUsername,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseApi.buildResponse(HttpStatus.OK, userService.changeUsername(requestNewUsername, userDetails));
    }

    @Operation(
            summary = "Password reset",
            description = "Generates a new password for the user and sends it by email.",
            parameters = @Parameter(
                    name = "email",
                    description = "Email of the user whose password must be reset",
                    required = true,
                    example = "user@example.com"
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid email parameter", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @PatchMapping("auth/users/change/reset")
    public ResponseEntity<ResponseApi<String>> resetPassword(@RequestParam @Email @NotBlank String email) {
        return ResponseApi.buildResponse(HttpStatus.OK, userService.resetPassword(email));
    }

    @Operation(
            summary = "User list",
            description = "Returns a paginated list of all users in the system.",
            parameters = {
                    @Parameter(name = "pageNumber", description = "Page number", example = "0"),
                    @Parameter(name = "pageSize", description = "Number of items per page", example = "10"),
                    @Parameter(name = "name", description = "Field used to sort the results", example = "id"),
                    @Parameter(name = "direction", description = "Sort direction: ASC or DESC", example = "ASC")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "User list retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @GetMapping("administrator/users")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<ResponseApi<Map<String, Object>>> usersList(
            @RequestParam(defaultValue = "0") @Min(0) int pageNumber,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize,
            @RequestParam(defaultValue = "id") @Size(min = 1, max = 50) String name,
            @RequestParam(defaultValue = "ASC") @Size(min = 3, max = 4) String direction
    ) {
        Map<String, Object> page = BuildPageableResponse.buildPage(
                userService.usersList(pageNumber, pageSize, name, direction)
        );
        return ResponseApi.buildResponse(HttpStatus.OK, page);
    }

    @Operation(
            summary = "Disable a user",
            description = "Logically deletes a user by disabling the account. Existing transactions remain visible.",
            parameters = @Parameter(
                    name = "userId",
                    description = "ID of the user to disable",
                    required = true,
                    example = "1"
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User disabled successfully", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid userId parameter", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "403", description = "Unauthorized operation", content = @Content(schema = @Schema(implementation = ResponseApi.class))),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ResponseApi.class)))
            }
    )
    @DeleteMapping("administrator/users/{userId}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<ResponseApi<String>> disableUser(
            @PathVariable @NotNull @Positive Integer userId
    ) {
        return ResponseApi.buildResponse(HttpStatus.OK, userService.disableUser(userId));
    }
}
