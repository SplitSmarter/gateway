package com.onewordmax.splitsmart.gateway.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.SecurityConstants;
import com.onewordmax.splitsmart.gateway.dto.ErrorResponse;
import com.onewordmax.splitsmart.gateway.exceptions.AuthenticationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class ServerAuthenticationEntryPointConfig implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> commence(
            ServerWebExchange exchange,
            AuthenticationException ex
    ) {
        String path = exchange.getRequest().getPath().value();
        log.warn("Authentication failure entry point triggered, path={}, exceptionType={}, message={}",
                path, ex.getClass().getSimpleName(), ex.getMessage());

        HttpStatus status;
        String errorCode;
        Object meta = null;
        String messageKey = ErrorCode.SERVICE_UNAUTHENTICATED;

        if (ex instanceof AuthenticationFailedException afe) {
            status = afe.getStatus();
            errorCode = afe.getErrorCode();
            meta = afe.getMeta();
            messageKey = afe.getMessage(); // i18n key
            log.debug("Specialized AuthenticationFailedException caught, errorCode={}, status={}", errorCode, status);
        } else {
            errorCode = ErrorCode.SERVICE_UNAUTHENTICATED;
            status = HttpStatus.UNAUTHORIZED;
        }

        ErrorResponse<String, Object> response = new ErrorResponse<>();
        response.setMessage(messageKey);
        response.setError(errorCode);
        response.setMeta(meta);

        String traceId = exchange.getRequest().getHeaders().getFirst(SecurityConstants.TRACE_ID_HEADER_NAME);
        if (traceId == null) {
            traceId = exchange.getResponse().getHeaders().getFirst(SecurityConstants.TRACE_ID_HEADER_NAME);
        }

        if (traceId != null) {
            log.debug("Attaching traceId to error response headers, traceId={}", traceId);
            exchange.getResponse().getHeaders().set(SecurityConstants.TRACE_ID_HEADER_NAME, traceId);
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        return exchange.getResponse().writeWith(Mono.fromCallable(() -> {
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(response);
                log.info("Sending error response to client, path={}, status={}, errorCode={}", path, status, errorCode);
                return exchange.getResponse().bufferFactory().wrap(bytes);
            } catch (Exception e) {
                log.error("Failed to serialize error response, path={}, error={}", path, e.getMessage());
                throw e;
            }
        }));
    }
}
