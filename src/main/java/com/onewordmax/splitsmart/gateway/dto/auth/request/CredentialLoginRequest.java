package com.onewordmax.splitsmart.gateway.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public record CredentialLoginRequest (
    String emailOrUsername,
    String password
) {}
