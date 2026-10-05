package com.onewordmax.splitsmart.gateway.controller;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.CredentialSignupRequest;
import com.onewordmax.splitsmart.gateway.dto.auth.request.ResendOtpRequest;
import com.onewordmax.splitsmart.gateway.dto.auth.request.SendOtpRequest;
import com.onewordmax.splitsmart.gateway.dto.auth.request.VerifyOtpRequest;
import com.onewordmax.splitsmart.gateway.exceptions.ServiceException;
import com.onewordmax.splitsmart.gateway.service.OtpService;
import com.onewordmax.splitsmart.gateway.validation.request.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final SendOtpCleaner sendOtpCleaner;
    private final SendOtpValidator sendOtpValidator;
    private final VerifyOtpCleaner verifyOtpCleaner;
    private final VerifyOtpValidator verifyOtpValidator;
    private final ResendOtpCleaner resendOtpCleaner;
    private final ResendOtpValidator resendOtpValidator;


    @PostMapping("/send")
    public Mono<ResponseEntity<?>> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            ServerHttpRequest httpRequest
    ) {
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
        SendOtpRequest cleaned = sendOtpCleaner.clean(request);
        List<ValidationError> errors = sendOtpValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }

        return otpService.sendOtp(
                cleaned.email(),
                ip,
                userAgent
        );
    }

    @PostMapping("/verify")
    public Mono<ResponseEntity<?>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            ServerHttpRequest httpRequest
    ) {
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
        VerifyOtpRequest cleaned = verifyOtpCleaner.clean(request);
        List<ValidationError> errors = verifyOtpValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }

        return otpService.verifyOtp(
                cleaned.email(),
                cleaned.otpCode(),
                ip,
                userAgent
        );
    }

    @PostMapping("/resend")
    public Mono<ResponseEntity<?>> resendOtp(
            @Valid @RequestBody ResendOtpRequest request,
            ServerHttpRequest httpRequest
    ) {
        String ip = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeaders().getFirst("User-Agent");
        ResendOtpRequest cleaned = resendOtpCleaner.clean(request);
        List<ValidationError> errors = resendOtpValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }

        return otpService.resendOtp(
                cleaned.email(),
                ip,
                userAgent
        );
    }

    /**
     * Extracts client IP in a reactive context.
     * Checks X-Forwarded-For for proxy support.
     */
    private String extractClientIp(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().getFirst("X-Forwarded-For"))
                .map(forwarded -> forwarded.split(",")[0].trim())
                .orElseGet(() -> Optional.ofNullable(request.getRemoteAddress())
                        .map(addr -> addr.getAddress().getHostAddress())
                        .orElse("unknown"));
    }
}