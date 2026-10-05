package com.onewordmax.splitsmart.gateway.exceptions;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationFailedException extends AuthenticationException {

    private final HttpStatus status;
    private final String errorCode;
    private final Object meta;

    public AuthenticationFailedException(
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

    public AuthenticationFailedException() {
        this(
                ErrorCode.SERVICE_UNAUTHENTICATED,
                ErrorCode.SERVICE_UNAUTHENTICATED,
                HttpStatus.UNAUTHORIZED,
                null
        );
    }

    public AuthenticationFailedException(String message, String errorCode) {
        this(errorCode, message, HttpStatus.UNAUTHORIZED, null);
    }

}
