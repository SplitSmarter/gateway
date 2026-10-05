package com.onewordmax.splitsmart.gateway.controller;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.URIConstants;
import com.onewordmax.splitsmart.gateway.dto.ErrorResponse;
import com.onewordmax.splitsmart.gateway.dto.SuccessResponse;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.*;
import com.onewordmax.splitsmart.gateway.dto.auth.response.LoginResponse;
import com.onewordmax.splitsmart.gateway.dto.auth.response.SignupResponse;
import com.onewordmax.splitsmart.gateway.exceptions.ServiceException;
import com.onewordmax.splitsmart.gateway.repositories.UserRepository;
import com.onewordmax.splitsmart.gateway.service.GoogleAuthServices;
import com.onewordmax.splitsmart.gateway.service.UserService;
import com.onewordmax.splitsmart.gateway.utils.jwt.JwtUtils;
import com.onewordmax.splitsmart.gateway.validation.request.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static com.onewordmax.splitsmart.gateway.constants.SecurityConstants.REFRESH_TOKEN_COOKIE_NAME;

// TODO: Ensure external service are not working error is thrown during runtime instead of silently failing, occured with docker

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final GoogleAuthServices googleAuthServices;
    private final UserRepository userRepository;

    @Value("${token.validity.refresh}")
    private int refreshTokenValidity;

    private final CredentialSignupCleaner signupCleaner;
    private final CredentialSignupValidator signupValidator;
    private final CredentialLoginCleaner loginCleaner;
    private final CredentialLoginValidator loginValidator;
    private final GoogleSignupCleaner googleSignupCleaner;
    private final GoogleSignupValidator googleSignupValidator;
    private final GoogleLoginValidator googleLoginValidator;
    private final GoogleLoginCleaner googleLoginCleaner;
    private final PasswordResetRequestCleaner passwordResetCleaner;
    private final PasswordResetRequestValidator passwordResetValidator;
    private final PasswordResetVerifyCleaner passwordResetVerifyCleaner;
    private final PasswordResetVerifyValidator PasswordResetVerifyValidator;

    private ResponseCookie getResponseTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .path("/api/gateway/token/access")
                .maxAge(Duration.ofSeconds(refreshTokenValidity))
                .build();
    }

    @PostMapping(URIConstants.SIGNUP_CREDENTIALS)
    public Mono<ResponseEntity<?>> userSignup(@RequestBody CredentialSignupRequest requestDto) {
        CredentialSignupRequest cleaned = signupCleaner.clean(requestDto);
        List<ValidationError> errors = signupValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }

        return userService.userSignup(cleaned)
                .flatMap(responseEntity -> {
                    // If signup wasn't 201 Created (though errors should be exceptions now), return as is
                    if (responseEntity.getStatusCode() != HttpStatus.CREATED) {
                        return Mono.just(responseEntity);
                    }

                    SuccessResponse<?, ?> successBody = (SuccessResponse<?, ?>) responseEntity.getBody();
                    if (successBody == null || !(successBody.getData() instanceof SignupResponse signupData)) {
                        log.error("Unexpected response body type from userService.userSignup");
                        return Mono.just(responseEntity);
                    }

                    String uniqueUsername = signupData.getUsername();
                    log.info("Generating security context for username: {}", uniqueUsername);

                    return userRepository.findByUsername(uniqueUsername)
                            .map(user -> {
                                // Generate Tokens
                                log.debug("Generating tokens for userId: {}", user.getId());
                                String refreshToken = jwtUtils.generateRefreshToken(String.valueOf(user.getId()), user.getEmail());
                                String rtSubject = jwtUtils.extractSubjectSync(refreshToken);
                                String accessToken = jwtUtils.generateAccessToken(
                                        String.valueOf(user.getId()),
                                        String.valueOf(user.getRole()),
                                        rtSubject
                                );

                                ResponseCookie refreshTokenCookie = getResponseTokenCookie(refreshToken);
                                log.info("Signup flow complete for user: {}. Tokens generated.", user.getId());

                                // Return the original SuccessResponse but with Security Headers
                                return ResponseEntity.status(HttpStatus.CREATED)
                                        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                        .body(responseEntity.getBody());
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("Critical Error: User with username {} was reported created but not found in DB", uniqueUsername);
                                return Mono.error(new ServiceException(
                                        ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                                        ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                                        HttpStatus.INTERNAL_SERVER_ERROR)
                                );
                            }));
                });
    }

    @PostMapping(URIConstants.LOGIN_CREDENTIALS)
    public Mono<ResponseEntity<?>> userLogin(@RequestBody CredentialLoginRequest requestDto) {
        CredentialLoginRequest cleaned = loginCleaner.clean(requestDto);
        List<ValidationError> errors = loginValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }

        log.info("Login request received for identifier: {}", cleaned.emailOrUsername());

        return userService.userLogin(cleaned)
                .flatMap(responseEntity -> {
                    // 1. If service returned an error (401, 404, etc.), return it directly
                    if (responseEntity.getStatusCode() != HttpStatus.OK) {
                        return Mono.just(responseEntity);
                    }

                    // 2. Extract Data from Successful Login
                    SuccessResponse<?, ?> successBody = (SuccessResponse<?, ?>) responseEntity.getBody();
                    if (successBody == null || !(successBody.getData() instanceof LoginResponse loginData)) {
                        log.error("Unexpected response body type from userService.userLogin");
                        return Mono.just(responseEntity);
                    }

                    log.info("User {} authenticated, generating security tokens", loginData.getUsername());

                    // 3. Generate Tokens
                    return userRepository.findById(loginData.getId())
                            .map(user -> {
                                String refreshToken = jwtUtils.generateRefreshToken(
                                        String.valueOf(user.getId()),
                                        user.getEmail()
                                );
                                String rtSubject = jwtUtils.extractSubjectSync(refreshToken);
                                String accessToken = jwtUtils.generateAccessToken(
                                        String.valueOf(user.getId()),
                                        String.valueOf(user.getRole()),
                                        rtSubject
                                );

                                ResponseCookie refreshTokenCookie = getResponseTokenCookie(refreshToken);
                                log.info("Login successful for user, id={}", user.getId());

                                return ResponseEntity.ok()
                                        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                        .body(responseEntity.getBody());
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.error("Critical: User ID {} found in service but not in database during login", loginData.getId());
                                return Mono.error(new ServiceException(
                                        ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                                        ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                                        HttpStatus.INTERNAL_SERVER_ERROR));
                            }));
                });
    }

    @PostMapping(URIConstants.LOGIN_GOOGLE)
    public Mono<ResponseEntity<?>> googleLogin(@RequestBody GoogleLoginRequest request) {
        GoogleLoginRequest cleaned = googleLoginCleaner.clean(request);
        List<ValidationError> errors = googleLoginValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }
        return userService.googleLogin(cleaned.idToken())
                .flatMap(responseEntity -> handleAuthSuccess(responseEntity, HttpStatus.OK));
    }

    @PostMapping(URIConstants.SIGNUP_GOOGLE)
    public Mono<ResponseEntity<?>> googleSignup(@RequestBody GoogleSignupRequest request) {
        GoogleSignupRequest cleaned = googleSignupCleaner.clean(request);
        List<ValidationError> errors = googleSignupValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }
        return userService.googleSignup(cleaned)
                .flatMap(responseEntity -> handleAuthSuccess(responseEntity, HttpStatus.CREATED));
    }


    private Mono<ResponseEntity<?>> handleAuthSuccess(ResponseEntity<?> responseEntity,
                                                      HttpStatus successStatus) {
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            return Mono.just(responseEntity);
        }

        SuccessResponse<?, ?> successBody = (SuccessResponse<?, ?>) responseEntity.getBody();
        if (successBody == null) return Mono.just(responseEntity);

        Long userId;
        try {
            Object data = successBody.getData();
            if (data instanceof SignupResponse d) userId = d.getId();
            else if (data instanceof LoginResponse d) userId = d.getId();
            else throw new Exception("Unknown data type");
        } catch (Exception e) {
            log.error("Failed to extract user ID from response data");
            return Mono.just(responseEntity);
        }

        return userRepository.findById(userId)
                .map(user -> {
                    String refreshToken = jwtUtils.generateRefreshToken(String.valueOf(user.getId()), user.getEmail());
                    String rtSubject = jwtUtils.extractSubjectSync(refreshToken);
                    String accessToken = jwtUtils.generateAccessToken(
                            String.valueOf(user.getId()),
                            user.getRole().name(),
                            rtSubject
                    );

                    ResponseCookie refreshTokenCookie = getResponseTokenCookie(refreshToken);

                    return ResponseEntity.status(successStatus)
                            .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .body(responseEntity.getBody());
                });
    }

    @PostMapping("/password/reset/request")
    public Mono<ResponseEntity<?>> requestPasswordReset(
            @RequestBody PasswordResetRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestHeader(value = HttpHeaders.USER_AGENT) String userAgent
    ) {
        log.info("Received password reset request for email: {}", request.email());
        PasswordResetRequest cleaned = passwordResetCleaner.clean(request);
        List<ValidationError> errors = passwordResetValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }


        // Use the IP from header if behind a proxy, otherwise local context
        String clientIp = (ip != null && !ip.isEmpty()) ? ip.split(",")[0] : "unknown";

        return userService.requestPasswordReset(cleaned.email(), clientIp, userAgent);
    }

    @PostMapping("/password/reset/verify")
    public Mono<ResponseEntity<?>> verifyPasswordReset(
            @RequestBody PasswordResetVerifyRequest request,
            @RequestHeader(value = HttpHeaders.USER_AGENT) String userAgent
    ) {
        log.info("Received password reset verification for token: {}", request.token());
        PasswordResetVerifyRequest cleaned = passwordResetVerifyCleaner.clean(request);
        List<ValidationError> errors = PasswordResetVerifyValidator.validate(cleaned);
        if (!errors.isEmpty()) {
            throw new ServiceException(
                    ErrorCode.REQUEST_INVALID,
                    ErrorCode.REQUEST_INVALID,
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    errors
            );
        }

        return userService.verifyAndResetPassword(cleaned.token(), cleaned.newPassword());
    }
}
