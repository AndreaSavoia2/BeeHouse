package com.prj.beehouse.payload.request;

import com.prj.beehouse.util.StringUtility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegistrationRequest {

    @Size(min = 1, max = 50)
    @NotBlank
    @Schema(description = "User first name", example = "John", required = true)
    private String name;

    @Size(min = 1, max = 50)
    @NotBlank
    @Schema(description = "User last name", example = "Doe", required = true)
    private String lastname;

    @Email
    @NotBlank
    @Schema(description = "User email, also used for login and password reset", example = "john.doe@email.com", required = true)
    private String email;

    public static RegistrationRequest clean(RegistrationRequest request) {
        return RegistrationRequest.builder()
                .name(StringUtility.cleanString(request.getName()))
                .lastname(StringUtility.cleanString(request.getLastname()))
                .email(StringUtility.cleanString(request.getEmail()))
                .build();
    }

}
