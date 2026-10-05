package com.onewordmax.splitsmart.gateway.controller;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.SecurityConstants;
import com.onewordmax.splitsmart.gateway.exceptions.ServiceException;
import com.onewordmax.splitsmart.gateway.utils.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.onewordmax.splitsmart.gateway.constants.SecurityConstants.USER_ID_HEADER_NAME;

@Slf4j
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class UserProxyController {

    private final WebClient userServiceWebClient;
    private final JwtUtils jwtUtils;

    @RequestMapping("/**")
    public Mono<ResponseEntity<byte[]>> proxyUserRequests(
            ServerWebExchange exchange,
            @RequestBody(required = false) byte[] body
    ) {
        ServerHttpRequest request = exchange.getRequest();

        String fullPath = request.getPath().pathWithinApplication().value();
        String targetPath = fullPath.replaceFirst("^/api/internal/users", "");
        HttpMethod method = request.getMethod();

        log.info("Proxying {} request to: {}", method, targetPath);

        return Mono.deferContextual(ctx -> {
            String traceId = ctx.getOrDefault(SecurityConstants.TRACE_ID_CONTEXT_KEY, "unknown");
            String businessUserId = exchange.getAttribute(SecurityConstants.USER_ID_HEADER_NAME);

            log.info("Proxying {} to: {}, traceId={}", method, targetPath, traceId);

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.error(new ServiceException(ErrorCode.SERVICE_UNAUTHENTICATED, ErrorCode.SERVICE_UNAUTHENTICATED, HttpStatus.UNAUTHORIZED));
            }

            return jwtUtils.extractId(authHeader.substring(7))
                    .flatMap(internalId -> userServiceWebClient
                            .method(method)
                            .uri(uriBuilder -> uriBuilder.path(targetPath).queryParams(request.getQueryParams()).build())
                            .headers(headers -> {
                                headers.addAll(request.getHeaders());
                                headers.set(USER_ID_HEADER_NAME, businessUserId);
                            })
                            .bodyValue(body != null ? body : new byte[0])
                            .exchangeToMono(clientResponse -> {
                                if (clientResponse.statusCode().equals(HttpStatus.UNAUTHORIZED)) {
                                    log.error("Downstream service rejected authentication for user: {}. This implies a sync issue between Gateway and Microservice.", businessUserId);

                                    return Mono.error(new ServiceException(
                                            ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                                            ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                                            HttpStatus.INTERNAL_SERVER_ERROR));
                                }

                                return clientResponse.bodyToMono(byte[].class)
                                        .defaultIfEmpty(new byte[0])
                                        .map(responseBody -> ResponseEntity
                                                .status(clientResponse.statusCode())
                                                .headers(clientResponse.headers().asHttpHeaders())
                                                .body(responseBody));
                            })
                            .doOnError(e -> log.error("Proxy error: {}", e.getMessage())));
        });
    }
}