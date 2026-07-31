package com.king.ledgerengine.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDto {
    @Schema(description = "User's firstName", example = "John")
    @NotNull
    private String firstName;

    @Schema(description = "User's firstName", example = "Dor")
    @NotNull
    private String lastName;

    @Schema(description = "User's email address", example = "johndoe@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Password, minimum 8 characters", example = "SecurePass123")
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
