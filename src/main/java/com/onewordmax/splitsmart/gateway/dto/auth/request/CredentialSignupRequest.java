package com.onewordmax.splitsmart.gateway.dto.auth.request;

public record CredentialSignupRequest (
    String name,
    String email,
    String password,
    String city,
    String country,
    String currency,
    String language,
    String user_type
) {}
