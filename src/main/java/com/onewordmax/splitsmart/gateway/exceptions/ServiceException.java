package com.onewordmax.splitsmart.gateway.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ServiceException represents domain / business rule violations.
 */
@Getter
public class ServiceException extends ApiException {

    public ServiceException(
            String messageKey,
            String errorCode
    ) {
        super(messageKey, errorCode, HttpStatus.BAD_REQUEST);
    }

    public ServiceException(
            String messageKey,
            String errorCode,
            HttpStatus status
    ) {
        super(messageKey, errorCode, status);
    }

    public ServiceException(
            String messageKey,
            String errorCode,
            HttpStatus status,
            Object meta
    ) {
        super(messageKey, errorCode, status, meta);
    }
}
