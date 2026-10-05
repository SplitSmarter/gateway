package com.onewordmax.splitsmart.gateway.utils;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.exceptions.ServiceException;
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
public class MailUtils {

    private final WebClient webClient;

    @Autowired
    public MailUtils(@Qualifier("mailServiceWebClient") WebClient mailServiceWebClient) {
        this.webClient = mailServiceWebClient;
    }

    public Mono<Map<String, Object>> sendSignupOtp(String recipient, String otpCode) {
        log.info("Preparing to send Signup OTP to email: {}", recipient);
        Map<String, Object> payload = new HashMap<>();
        payload.put("recipient", recipient);
        payload.put("expiry_minutes", 10);
        payload.put("otp", otpCode);

        return postRequest("/otp/signup", payload, recipient, "OTP email");
    }

    public Mono<Map<String, Object>> sendResetPasswordMail(String recipient, String reset_link) {
        log.info("Preparing to send Reset Password email to: {}", recipient);
        Map<String, Object> payload = new HashMap<>();
        payload.put("recipient", recipient);
        payload.put("reset_link", reset_link);

        return postRequest("/auth/v1/reset-password", payload, recipient, "reset password mail");
    }

    /* -------------------- PRIVATE HELPER -------------------- */

    /**
     * Centralized method to handle common WebClient POST logic and error mapping.
     */
    private Mono<Map<String, Object>> postRequest(String uri, Map<String, Object> payload, String recipient, String actionName) {
        return webClient.post()
                .uri(uri)
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(), clientResponse ->
                        clientResponse.bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                                .flatMap(body -> {
                                    log.error("Mail Service error for {}. Status: {}, Body: {}", actionName, clientResponse.statusCode(), body);
                                    return Mono.error(new ServiceException(
                                            ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                                            ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                                            HttpStatus.INTERNAL_SERVER_ERROR
                                    ));
                                })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(res -> log.info("Successfully requested {} for {}", actionName, recipient))
                .doOnError(err -> {
                    if (!(err instanceof ServiceException)) {
                        log.error("Failed to communicate with Mail Service for {} (recipient {}): {}", actionName, recipient, err.getMessage());
                    }
                });
    }
}