package com.king.ledgerengine.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDto {
    @Schema(description = "User's email address", example = "jane@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Password, minimum 8 characters", example = "SecurePass123")
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}