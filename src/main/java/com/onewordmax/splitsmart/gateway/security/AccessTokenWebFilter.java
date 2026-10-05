package com.onewordmax.splitsmart.gateway.security;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.SecurityConstants;
import com.onewordmax.splitsmart.gateway.exceptions.AuthenticationFailedException;
import com.onewordmax.splitsmart.gateway.model.User;
import com.onewordmax.splitsmart.gateway.service.UserService;
import com.onewordmax.splitsmart.gateway.utils.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AccessTokenWebFilter implements WebFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final ServerAuthenticationEntryPoint unauthorizedHandler;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String token = resolveToken(exchange);

        if (token == null) {
            log.debug("No token found, continuing chain to let Security handle it, path={}", path);
            return chain.filter(exchange); // Let .authenticated() trigger the EntryPoint
        }

        return validateToken(token)
                .flatMap(user -> authenticate(exchange, chain, user))
                .onErrorResume(e -> {
                    AuthenticationException authEx = (e instanceof AuthenticationException)
                            ? (AuthenticationException) e
                            : new AuthenticationFailedException(ErrorCode.RESOURCE_TOKEN_INVALID, ErrorCode.RESOURCE_TOKEN_INVALID);

                    log.warn("Access token Security Filter caught error {}, path={}", authEx.getMessage(), path);

                    return unauthorizedHandler.commence(exchange, authEx);
                });
    }

    private Mono<User> validateToken(String token) {
        return jwtUtils.validateToken(token)
                .doOnError(e -> log.warn("JWT validation failed, error={}", e.getMessage()))
                .then(Mono.defer(() ->
                    jwtUtils.extractId(token).flatMap( userId -> {
                        log.debug("Extracted userId from token, userId={}", userId);
                        return userService.validateUser(userId)
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.warn("User validation failed (not found or inactive), userId={}", userId);
                                    return Mono.error(new AuthenticationFailedException());
                                }));
                    })));
    }

    private Mono<Void> authenticate(
            ServerWebExchange exchange,
            WebFilterChain chain,
            User user
    ) {
        log.info("Authentication successful, username={}, businessId={}", user.getUsername(), user.getUserId());
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        user.getUserId(),
                        null,
                        List.of()
                );
        exchange.getAttributes().put(SecurityConstants.USER_ID_HEADER_NAME, user.getUserId());
        return chain.filter(exchange)
                .contextWrite(
                        ReactiveSecurityContextHolder.withAuthentication(auth)
                );
    }

    private String resolveToken(ServerWebExchange exchange) {
        String header =
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return (header != null && header.startsWith("Bearer "))
                ? header.substring(7)
                : null;
    }
}
