package com.onewordmax.splitsmart.gateway.exceptions;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import org.springframework.http.HttpStatus;

public class TokenValidationException extends ApiException {

    public TokenValidationException() {
        super(
                ErrorCode.RESOURCE_TOKEN_EXPIRED,
                ErrorCode.RESOURCE_TOKEN_EXPIRED,
                HttpStatus.UNAUTHORIZED
        );
    }
}
