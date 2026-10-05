package com.onewordmax.splitsmart.gateway.exceptions;
import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ServiceException.class)
    public Mono<ResponseEntity<ErrorResponse<String, Object>>> handleServiceException(
            ServiceException ex,
            ServerWebExchange exchange
    ) {
        log.warn("ServiceException caught: {} - {}", ex.getErrorCode(), ex.getMessage());

        return Mono.just(ErrorResponse.of(
                ex.getStatus(),
                ex.getMessage(),
                ex.getErrorCode(),
                ex.getMeta()
        ));
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse<String, Object>>> handleResponseStatusException(
            org.springframework.web.server.ResponseStatusException ex,
            ServerWebExchange exchange
    ) {
        log.debug("ResponseStatusException caught: {} - {}", ex.getReason(), ex.getMessage());
        // If the cause is our ServiceException, delegate to that logic
        if (ex.getCause() instanceof ServiceException se) {
            return handleServiceException(se, exchange);
        }

        if (ex.getCause() instanceof RemoteServiceException se) {
            return handleRemoteServiceException(se, exchange);
        }

        if (ex.getCause() instanceof ApiException se) {
            return handleApiException(se, exchange);
        }


        return Mono.just(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                null
        ));
    }

    @ExceptionHandler(ApiException.class)
    public Mono<ResponseEntity<ErrorResponse<String, Object>>> handleApiException(
            ApiException ex,
            ServerWebExchange exchange
    ) {
        log.debug("ApiException caught: {} - {}", ex.getErrorCode(), ex.getMessage());
        ResponseEntity<ErrorResponse<String, Object>> entity = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.UNKNOWN_ERROR,
                ErrorCode.UNKNOWN_ERROR,
                null
        );

        return Mono.just(entity);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public Mono<ResponseEntity<ErrorResponse<String, Object>>> handleAuthenticationFailedException(
            AuthenticationFailedException ex,
            ServerWebExchange exchange
    ) {
        log.debug("AuthenticationFailedException caught: {} - {}", ex.getErrorCode(), ex.getMessage());
        ResponseEntity<ErrorResponse<String, Object>> entity = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.SERVICE_UNAUTHENTICATED,
                ErrorCode.SERVICE_UNAUTHENTICATED,
                null
        );

        return Mono.just(entity);
    }

    @ExceptionHandler(RemoteServiceException.class)
    public Mono<ResponseEntity<ErrorResponse<String, Object>>> handleRemoteServiceException(
            ApiException ex,
            ServerWebExchange exchange
    ) {
        log.debug("RemoteServiceException caught: {} - {}", ex.getErrorCode(), ex.getMessage());
        ResponseEntity<ErrorResponse<String, Object>> entity = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.UNKNOWN_ERROR,
                ErrorCode.UNKNOWN_ERROR,
                null
        );

        return Mono.just(entity);
    }


    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse<String, Object>>> handleUnknown(
            Exception ex,
            ServerWebExchange exchange
    ) {
        log.error("Unknown Exception of type [{}]: {}", ex.getClass().getName(), ex.getMessage(), ex);
//        Locale locale = exchange.getAttributeOrDefault("locale", Locale.ENGLISH);
//
//        String key = ErrorCode.HTTP_INTERNAL_SERVER;
//        String translated = messageSource.getMessage(key, null, key, locale);
        return Mono.just(
                ErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        ErrorCode.UNKNOWN_ERROR,
                        ErrorCode.UNKNOWN_ERROR,
                        null
                )
        );
    }
}
