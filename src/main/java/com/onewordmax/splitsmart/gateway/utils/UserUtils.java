package com.onewordmax.splitsmart.gateway.utils;

import com.onewordmax.splitsmart.gateway.dto.external.RemoteAddUserRequest;
import com.onewordmax.splitsmart.gateway.dto.external.RemoteAddUserResponse;
import com.onewordmax.splitsmart.gateway.dto.external.RemoteSuccessResponse;
import com.onewordmax.splitsmart.gateway.dto.external.RemoteUserProfileResponse;
import com.onewordmax.splitsmart.gateway.exceptions.RemoteServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UserUtils {

    private final WebClient webClient;

    public UserUtils(@Qualifier("userServiceWebClient") WebClient userServiceWebClient) {
        this.webClient = userServiceWebClient;
    }

    public Mono<RemoteAddUserResponse> createUser(RemoteAddUserRequest request) {
        return webClient.post()
                .uri("/user/v1/")
                .bodyValue(request) // Spring automatically serializes the object to JSON
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(Map.class)
                                .flatMap(error -> Mono.error(new RemoteServiceException(
                                        "User Service Error",
                                        error.toString(),
                                        HttpStatus.INTERNAL_SERVER_ERROR)))
                )
                .bodyToMono(new ParameterizedTypeReference<RemoteSuccessResponse<RemoteAddUserResponse, Object>>() {})
                .map(RemoteSuccessResponse::getData);
    }

    public Mono<Void> confirmUser(String remoteUserId) {
        return webClient.post()
                .uri("/user/v1/confirm" + "?user_id=" + remoteUserId)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(Map.class)
                                .flatMap(error -> {
                                    log.error("Failed to confirm user in Microservice: {}", error);
                                    return Mono.error(new RemoteServiceException(
                                            "User Confirmation Failed",
                                            error.toString(),
                                            HttpStatus.INTERNAL_SERVER_ERROR));
                                })
                )
                .bodyToMono(Void.class);
    }

    public Mono<RemoteUserProfileResponse> getInternalUserProfile(Long remoteUserId, String userType) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/v1/internal")
                        .queryParam("user_id", remoteUserId)
                        .queryParam("user_type", userType)
                        .build())
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), response ->
                        response.bodyToMono(Map.class)
                                .flatMap(error -> {
                                    log.error("Internal Profile Fetch Failed. Status: {}, Error: {}",
                                            response.statusCode(), error);
                                    return Mono.error(new RemoteServiceException(
                                            "Internal User Service Error",
                                            error.toString(),
                                            HttpStatus.valueOf(response.statusCode().value())));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<RemoteSuccessResponse<RemoteUserProfileResponse, Object>>() {})
                .map(RemoteSuccessResponse::getData);
    }
}