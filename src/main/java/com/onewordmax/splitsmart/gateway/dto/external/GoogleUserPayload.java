package com.onewordmax.splitsmart.gateway.dto.external;

public record GoogleUserPayload(
        String email,
        String name,
        String googleSubId // The unique identifier from Google
) {}