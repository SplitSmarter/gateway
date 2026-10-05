package com.onewordmax.splitsmart.gateway.config;

import com.onewordmax.splitsmart.gateway.security.AccessTokenWebFilter;
import com.onewordmax.splitsmart.gateway.security.RefreshTokenWebFilter;
import com.onewordmax.splitsmart.gateway.service.UserService;
import com.onewordmax.splitsmart.gateway.utils.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ServerAuthenticationEntryPoint unauthorizedHandler;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Bean
    @Order(1)
    public SecurityWebFilterChain publicSecurityChain(ServerHttpSecurity http) {
        return http
                .securityMatcher(pathMatchers("/api/auth/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().permitAll())
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityWebFilterChain gatewaySecurityChain(ServerHttpSecurity http) {

        RefreshTokenWebFilter refreshTokenWebFilter =
                new RefreshTokenWebFilter(jwtUtils, userService, unauthorizedHandler);

        return http
                .securityMatcher(pathMatchers("/api/gateway/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().authenticated())
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(unauthorizedHandler))
                .addFilterAt(refreshTokenWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .build();
    }

    @Bean
    @Order(3)
    public SecurityWebFilterChain internalApiSecurityChain(ServerHttpSecurity http) {

        AccessTokenWebFilter accessTokenWebFilter =
                new AccessTokenWebFilter(jwtUtils, userService, unauthorizedHandler);

        return http
                .securityMatcher(pathMatchers("/api/internal/**"))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex.anyExchange().authenticated())
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(unauthorizedHandler))
                .addFilterAt(accessTokenWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .build();
    }
}
