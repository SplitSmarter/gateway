package com.onewordmax.splitsmart.gateway.security;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.SecurityConstants;
import com.onewordmax.splitsmart.gateway.exceptions.AuthenticationFailedException;
import com.onewordmax.splitsmart.gateway.model.User;
import com.onewordmax.splitsmart.gateway.service.UserService;
import com.onewordmax.splitsmart.gateway.utils.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpCookie;
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

import static com.onewordmax.splitsmart.gateway.constants.SecurityConstants.REFRESH_TOKEN_COOKIE_NAME;

@Slf4j
@RequiredArgsConstructor
public class RefreshTokenWebFilter implements WebFilter {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final ServerAuthenticationEntryPoint unauthorizedHandler;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        String token = extractRefreshToken(exchange);

        if (token == null) {
            log.debug("No refresh token cookie found, continuing chain, path={}", path);
            return chain.filter(exchange);
        }

        return validateToken(token)
                .flatMap(user -> authenticate(exchange, chain, user, token))
                .onErrorResume(e -> {
                    log.warn("Refresh token Security Filter caught error, path={}, error={}", path, e.getMessage());

                    AuthenticationException authEx = (e instanceof AuthenticationException)
                            ? (AuthenticationException) e
                            : new AuthenticationFailedException(ErrorCode.RESOURCE_TOKEN_INVALID, ErrorCode.RESOURCE_TOKEN_INVALID);

                    return unauthorizedHandler.commence(exchange, authEx);
                });
    }

    private Mono<User> validateToken(String token) {
        return jwtUtils.validateToken(token)
                .doOnError(e -> log.warn("Refresh JWT validation failed, error={}", e.getMessage()))
                .then(Mono.defer(() -> {
                    // Extract ID from the refresh token
                    return jwtUtils.extractId(token)
                            .flatMap(userId -> {
                                log.debug("Extracted userId from refresh token, userId={}", userId);
                                return userService.validateUser(userId);
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.warn("User validation failed for refresh token (not found or inactive)");
                                return Mono.error(new AuthenticationFailedException(ErrorCode.RESOURCE_USER_EMPTY, ErrorCode.RESOURCE_USER_EMPTY));
                            }));
                }));
    }

    private Mono<Void> authenticate(ServerWebExchange exchange, WebFilterChain chain, User user, String token) {
        log.info("Refresh token authentication successful, username={}, userId={}", user.getUsername(), user.getId());
        exchange.getAttributes().put(SecurityConstants.USER_ID_HEADER_NAME, user.getUserId());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                token,
                List.of()
        );

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
    }

    private String extractRefreshToken(ServerWebExchange exchange) {
        HttpCookie cookie = exchange.getRequest()
                .getCookies()
                .getFirst(REFRESH_TOKEN_COOKIE_NAME);
        return cookie != null ? cookie.getValue() : null;
    }
}