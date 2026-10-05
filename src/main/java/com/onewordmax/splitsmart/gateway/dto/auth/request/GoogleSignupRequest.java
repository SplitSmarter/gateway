package com.onewordmax.splitsmart.gateway.dto.auth.request;

public record GoogleSignupRequest (
    String idToken,
    String name,
    String password,
    String city,
    String country,
    String currency,
    String language
) {}