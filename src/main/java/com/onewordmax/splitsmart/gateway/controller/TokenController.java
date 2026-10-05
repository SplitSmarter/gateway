package com.onewordmax.splitsmart.gateway.controller;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.SecurityConstants;
import com.onewordmax.splitsmart.gateway.constants.SuccessCode;
import com.onewordmax.splitsmart.gateway.dto.SuccessResponse;
import com.onewordmax.splitsmart.gateway.exceptions.ServiceException;
import com.onewordmax.splitsmart.gateway.repositories.UserRepository;
import com.onewordmax.splitsmart.gateway.service.UserService;
import com.onewordmax.splitsmart.gateway.utils.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/gateway/token")
@RequiredArgsConstructor
public class TokenController {

        private final JwtUtils jwtUtils;
        private final UserService userService;
        private final UserRepository userRepository;

        @GetMapping("/access")
        public Mono<ResponseEntity<?>> getAccessToken(ServerWebExchange exchange) {
                log.info("Request received in /access endpoint");

                return ReactiveSecurityContextHolder.getContext()
                        .map(SecurityContext::getAuthentication)
                        .flatMap(auth -> {
                                // The 'auth' principal here is likely the username (String)
                                // based on your filter's UsernamePasswordAuthenticationToken
                                String username = (String) auth.getPrincipal();

                                // Fetch the full user from DB to get the ID and Role for the new JWT
                                return userRepository.findByUsername(username)
                                        .flatMap(user -> {
                                                String refreshToken = (String) auth.getCredentials();

                                                return jwtUtils.extractSubject(refreshToken)
                                                        .map(subject -> {
                                                                String accessToken = jwtUtils.generateAccessToken(
                                                                        String.valueOf(user.getId()),
                                                                        user.getRole().name(),
                                                                        subject
                                                                );

                                                                return (ResponseEntity<?>) ResponseEntity.ok()
                                                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                                                        .body(SuccessResponse.of(
                                                                                HttpStatus.OK,
                                                                                SuccessCode.OK,
                                                                                null,
                                                                                null
                                                                        ));
                                                        });
                                        });
                        })
                        .<ResponseEntity<?>>map(r -> r)
                        .switchIfEmpty(Mono.error(new ServiceException(ErrorCode.SERVICE_UNAUTHENTICATED, ErrorCode.SERVICE_UNAUTHENTICATED, HttpStatus.UNAUTHORIZED)));
        }
}