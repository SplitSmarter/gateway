package com.onewordmax.splitsmart.gateway.dto.auth.request;

public record PasswordResetVerifyRequest (
        String newPassword,
        String token
) {}
