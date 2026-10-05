package com.onewordmax.splitsmart.gateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

public class RemoteServiceException extends ApiException {

    public RemoteServiceException(
            String messageKey,
            String errorCode,
            HttpStatus status
    ) {
        super(messageKey, errorCode, status, null);
    }
}
