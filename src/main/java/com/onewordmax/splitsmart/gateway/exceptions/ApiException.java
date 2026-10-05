package com.onewordmax.splitsmart.gateway.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object meta;

    protected ApiException(
            String messageKey,
            String errorCode,
            HttpStatus status
    ) {
        this(messageKey, errorCode, status, null);
    }

    protected ApiException(
            String messageKey,
            String errorCode,
            HttpStatus status,
            Object meta
    ) {
        super(messageKey);
        this.status = status;
        this.errorCode = errorCode;
        this.meta = meta;
    }
}
