package com.onewordmax.splitsmart.gateway.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "OTP code cannot be blank")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be a 6-digit number")
        String otpCode
) {}
