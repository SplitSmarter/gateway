package com.onewordmax.splitsmart.gateway.service;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.SuccessCode;
import com.onewordmax.splitsmart.gateway.constants.UserType;
import com.onewordmax.splitsmart.gateway.dto.ErrorResponse;
import com.onewordmax.splitsmart.gateway.dto.SuccessResponse;
import com.onewordmax.splitsmart.gateway.dto.auth.request.CredentialLoginRequest;
import com.onewordmax.splitsmart.gateway.dto.auth.request.CredentialSignupRequest;
import com.onewordmax.splitsmart.gateway.dto.auth.request.GoogleSignupRequest;
import com.onewordmax.splitsmart.gateway.dto.auth.response.LoginResponse;
import com.onewordmax.splitsmart.gateway.dto.auth.response.SignupResponse;
import com.onewordmax.splitsmart.gateway.dto.external.RemoteAddUserRequest;
import com.onewordmax.splitsmart.gateway.model.PasswordResetToken;
import com.onewordmax.splitsmart.gateway.model.SignupOtp;
import com.onewordmax.splitsmart.gateway.model.User;
import com.onewordmax.splitsmart.gateway.repositories.OtpRepository;
import com.onewordmax.splitsmart.gateway.repositories.PasswordResetRepository;
import com.onewordmax.splitsmart.gateway.repositories.UserRepository;
import com.onewordmax.splitsmart.gateway.utils.MailUtils;
import com.onewordmax.splitsmart.gateway.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    @Value("${otp.reverification.seconds}")
    private int reverificationSeconds;
    @Value("${reset-password.expiration.minutes}")
    private int resetExpiryMinutes;
    @Value("${reset-password.resend.limit}")
    private int resendLimit;
    @Value("${reset-password.resend.lock.seconds}")
    private int resendLockSeconds;

    // TODO: Avoid race condition in username
    // TODO: Add Guest user signup maybe
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthServices googleAuthServices;
    private final UserUtils userUtils;
    private final OtpRepository otpRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final MailUtils mailUtils;


    /* -------------------- SIGNUP -------------------- */

    @Transactional
    public Mono<ResponseEntity<?>> userSignup(CredentialSignupRequest request) {
        final String email = request.email();
        final String baseName = request.name().toLowerCase().replaceAll("\\s+", "");

        log.info("Starting user signup flow, email={}, baseName={}", email, baseName);

        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    log.warn("Signup failed, Email already exists in database, email={}, userId={}", email, user.getId());
                    return handleExistingUser(user);
                })
                .then(generateUniqueUsername(baseName))
                .flatMap(uniqueUsername -> createNewUserFlow(request, email, uniqueUsername));
    }

    private Mono<String> generateUniqueUsername(String base) {
        return generateUniqueUsername(base, 0);
    }

    private Mono<String> generateUniqueUsername(String base, int counter) {
        String candidate = (counter == 0) ? base : base + counter;

        return userRepository.existsByUsername(candidate)
                .flatMap(exists -> {
                    if (exists) {
                        // Recursive call: try next number
                        return generateUniqueUsername(base, counter + 1);
                    }
                    return Mono.just(candidate);
                });
    }

    private Mono<ResponseEntity<?>> handleExistingUser(User user) {
        return switch (user.getStatus()) {
            case ACTIVE, INACTIVE -> Mono.just(ErrorResponse.of(
                    HttpStatus.CONFLICT,
                    ErrorCode.RESOURCE_USER_CONFLICT,
                    ErrorCode.RESOURCE_USER_CONFLICT
            ));
            case BLOCKED -> Mono.just(ErrorResponse.of(
                    HttpStatus.FORBIDDEN,
                    ErrorCode.RESOURCE_ACCESS_DENIED,
                    ErrorCode.RESOURCE_ACCESS_DENIED
            ));
            case DELETED -> Mono.just(ErrorResponse.of(
                    HttpStatus.GONE,
                    ErrorCode.RESOURCE_USER_DELETED,
                    ErrorCode.RESOURCE_USER_DELETED
            ));
        };
    }

    private Mono<ResponseEntity<?>> createNewUserFlow(
            CredentialSignupRequest request,
            String email,
            String username
    ) {
        Instant now = Instant.now();
        if (email != null && !email.isBlank()) {
            return otpRepository.findByEmail(email)
                    .flatMap(otp -> {
                        boolean isExpired = (otp.getStatus() == SignupOtp.OtpStatus.USED &&
                                otp.getVerifiedAt().isBefore(now.minusSeconds(reverificationSeconds)));
                        boolean isNotVerified = otp.getStatus() != SignupOtp.OtpStatus.VERIFIED;
                        if (isExpired || isNotVerified) {
                            log.warn("Signup failed, OTP invalid or expired, email={}, status={}, verifiedAt={}",
                                    email, otp.getStatus(), otp.getVerifiedAt());
                            return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST, ErrorCode.FIELD_OTP_NOT_VERIFIED, ErrorCode.FIELD_OTP_NOT_VERIFIED));
                        }
                        return saveNewUser(request, email, username, otp);
                    })
                    .switchIfEmpty(Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST, ErrorCode.FIELD_OTP_EMPTY, ErrorCode.FIELD_OTP_EMPTY)));
        }
        return saveNewUser(request, null, username, null);
    }

    private Mono<ResponseEntity<?>> saveNewUser(
            CredentialSignupRequest request,
            String email,
            String username,
            SignupOtp otp
    ) {
        return Mono.fromCallable(() -> passwordEncoder.encode(request.password()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(hashedPassword -> {

                    // Build the typed request for the remote service
                    RemoteAddUserRequest remoteRequest = RemoteAddUserRequest.builder()
                            .name(request.name())
                            .email(email)
                            .city(request.city())
                            .country(request.country())
                            .currency(request.currency())
                            .language(request.language())
                            .userType(UserType.valueOf(request.user_type().toUpperCase()))
                            .build();

                    log.debug(remoteRequest.toString());

                    log.info("Calling remote user service, email={}, username={}", email, username);

                    return userUtils.createUser(remoteRequest)
                            .flatMap(remoteUser -> {
                                User user = new User();
                                user.setEmail(email);
                                user.setUsername(username);
                                user.setPassword(hashedPassword);
                                user.setUserId(remoteUser.getId().toString());
                                user.setStatus(User.Status.ACTIVE);
                                user.setAuthenticationMethod(User.AuthenticationMethod.CREDENTIALS);
                                user.setRole(User.Roles.USER);

                                return userRepository.save(user)
                                        .doOnNext(u -> log.debug("Saved local user record, id={}, username={}", u.getId(), u.getUsername()))
                                        .flatMap(savedUser -> {
                                            Mono<User> postSaveOp = Mono.just(savedUser);
                                            if (otp != null) {
                                                otp.setStatus(SignupOtp.OtpStatus.USED);
                                                postSaveOp = otpRepository.save(otp).thenReturn(savedUser);
                                            }

                                            // CHAIN THE CONFIRMATION CALL HERE
                                            return postSaveOp.flatMap(u ->
                                                    userUtils.confirmUser(u.getUserId())
                                                            .thenReturn(u)
                                            );
                                        })
                                        .flatMap(finalUser ->
                                                // ✅ Fetch the internal profile to get the correct DTO type
                                                userUtils.getInternalUserProfile(Long.valueOf(finalUser.getUserId()), finalUser.getRole().toString())
                                                        .map(profile -> SuccessResponse.of(
                                                                HttpStatus.CREATED,
                                                                SuccessCode.USER_CREATED,
                                                                new SignupResponse(
                                                                        finalUser.getId(),
                                                                        finalUser.getUsername(),
                                                                        profile
                                                                ),
                                                                null
                                                        ))
                                        );
                            })
                            .onErrorResume(e -> {
                                log.error("Signup failed during remote service call or DB save, email={}, error={}", email, e.getMessage());
                                return Mono.error(e);
                            });
                });
    }


    public Mono<User> validateUser(String id) {
        return userRepository.findByIdAndStatus(Long.parseLong(id), User.Status.ACTIVE);
    }


    /* -------------------- LOGIN -------------------- */

    public Mono<ResponseEntity<?>> userLogin(CredentialLoginRequest request) {
        String identifier = request.emailOrUsername();

        return getUserByEmailOrUsername(identifier)
                .flatMap(user -> checkPassword(request.password(), user))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Login failed: User not found, identifier={}", identifier);
                    return Mono.just(ErrorResponse.of(HttpStatus.NOT_FOUND, ErrorCode.REQUEST_INVALID, ErrorCode.REQUEST_INVALID));
                }))
                .onErrorResume(ex -> {
                    log.error("Unexpected error during login, identifier={}, error={}", identifier, ex.getMessage());
                    return Mono.just(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SERVICE_INTERNAL_SERVER_ERROR, ErrorCode.SERVICE_INTERNAL_SERVER_ERROR));
                });
    }

    private Mono<ResponseEntity<?>> checkPassword(String rawPassword, User user) {
        return Mono.fromCallable(() -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(match -> {
                    if (!match) {
                        log.warn("Login failed, Password mismatch, userId={}, username={}", user.getId(), user.getUsername());
                        return Mono.just(ErrorResponse.of(
                                HttpStatus.UNAUTHORIZED,
                                ErrorCode.REQUEST_INVALID,
                                ErrorCode.REQUEST_INVALID
                        ));
                    }
                    log.debug("Password verified, handling status, userId={}", user.getId());
                    return handleLoginStatus(user);
                });
    }

    /* -------------------- GOOGLE AUTH -------------------- */

    public Mono<ResponseEntity<?>> googleSignup(GoogleSignupRequest request) {
        log.info("Google signup initiated");
        return googleAuthServices.verifyToken(request.idToken())
                .flatMap(payload -> {
                    if (payload == null) {
                        log.warn("Google signup failed, Token verification returned null");
                        return Mono.just(ErrorResponse.of(HttpStatus.UNAUTHORIZED, ErrorCode.FIELD_GOOGLE_ID_TOKEN_INVALID, ErrorCode.FIELD_GOOGLE_ID_TOKEN_INVALID));
                    }

                    String email = payload.email().toLowerCase();
                    log.debug("Google token verified, email={}", email);

                    return userRepository.findByEmail(email)
                            .flatMap(user -> {
                                log.warn("Google signup failed: Email already exists, email={}", email);
                                return handleExistingUser(user);
                            })
                            .switchIfEmpty(generateUniqueUsername(request.name().toLowerCase().replaceAll("\\s+", ""))
                                    .flatMap(uniqueUsername -> {
                                        log.info("Creating new Google user, email={}, username={}", email, uniqueUsername);
                                        RemoteAddUserRequest remoteRequest = RemoteAddUserRequest.builder()
                                                .name(request.name())
                                                .email(email)
                                                .city(request.city())
                                                .country(request.country().toUpperCase())
                                                .currency(request.currency().toUpperCase())
                                                .language(request.language().toLowerCase())
                                                .userType(UserType.USER)
                                                .build();

                                        return userUtils.createUser(remoteRequest)
                                                .flatMap(remoteUser -> {
                                                    User user = new User();
                                                    user.setEmail(email);
                                                    user.setUsername(uniqueUsername);
                                                    user.setUserId(remoteUser.getId().toString());
                                                    user.setStatus(User.Status.ACTIVE);
                                                    user.setRole(User.Roles.USER);
                                                    user.setAuthenticationMethod(User.AuthenticationMethod.GOOGLE);

                                                    return userRepository.save(user)
                                                            .doOnNext(u -> log.info("Google user saved locally, id={}", u.getId()))
                                                            .flatMap(savedUser ->
                                                                    userUtils.confirmUser(savedUser.getUserId())
                                                                            .thenReturn(savedUser)
                                                            )
                                                            .flatMap(finalUser ->
                                                                    // ✅ Fetch the internal profile to get the correct DTO type
                                                                    userUtils.getInternalUserProfile(Long.valueOf(finalUser.getUserId()), finalUser.getRole().toString())
                                                                            .map(profile -> SuccessResponse.of(
                                                                                    HttpStatus.CREATED,
                                                                                    SuccessCode.USER_CREATED,
                                                                                    new SignupResponse(
                                                                                            finalUser.getId(),
                                                                                            finalUser.getUsername(),
                                                                                            profile
                                                                                    ),
                                                                                    null
                                                                            ))
                                                            );
                                                });
                                    }));
                });
    }

    /* -------------------- GOOGLE AUTH -------------------- */

    public Mono<ResponseEntity<?>> googleLogin(String token) {
        return googleAuthServices.verifyToken(token)
                .flatMap(payload -> {
                    if (payload == null) {
                        return Mono.just(ErrorResponse.of(
                                HttpStatus.UNAUTHORIZED,
                                ErrorCode.FIELD_GOOGLE_ID_TOKEN_INVALID,
                                ErrorCode.FIELD_GOOGLE_ID_TOKEN_INVALID
                        ));
                    }

                    String email = payload.email().toLowerCase();

                    return userRepository.findByEmail(email)
                            .flatMap(this::handleLoginStatus)
                            .switchIfEmpty(Mono.defer(() -> {
                                log.warn("No google account linked");
                                return Mono.just(ErrorResponse.of(
                                        HttpStatus.NOT_FOUND,
                                        ErrorCode.RESOURCE_USER_GOOGLE_UNLINKED,
                                        ErrorCode.RESOURCE_USER_GOOGLE_UNLINKED));
                            }));
                })
                .onErrorResume(ex -> {
                    log.error("Google Auth Error: {}", ex.getMessage());
                    return Mono.just(ErrorResponse.of(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            ErrorCode.SERVICE_INTERNAL_SERVER_ERROR,
                            ErrorCode.SERVICE_INTERNAL_SERVER_ERROR
                    ));
                });
    }

    /* -------------------- PASSWORD RESET -------------------- */

    public Mono<ResponseEntity<?>> requestPasswordReset(String email, String ip, String userAgent) {
        Instant now = Instant.now();

        return userRepository.findByEmail(email)
                .flatMap(user -> passwordResetRepository.findByEmail(email)
                        .flatMap(existing -> {
                            // 1. Check if Locked
                            if (existing.getLockUntil() != null && existing.getLockUntil().isAfter(now)) {
                                log.warn("Password reset is locked for email={}", email);
                                return Mono.just(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.FIELD_RESET_PASSWORD_LOCKED, ErrorCode.FIELD_RESET_PASSWORD_LOCKED));
                            }

                            // 2. Check for active token (not expired and not used)
                            if (!existing.isUsed() && existing.getExpiryTime().isAfter(now)) {
                                log.warn("Active reset token already exists for email={}", email);
                                return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST, ErrorCode.FIELD_RESET_PASSWORD_ALREADY_SENT, ErrorCode.FIELD_RESET_PASSWORD_ALREADY_SENT));
                            }

                            // 3. Handle Resend Limits
                            if (existing.getResendCount() >= resendLimit) {
                                log.warn("Max reset resend limit reached for email={}", email);
                                existing.setLockUntil(now.plusSeconds(resendLockSeconds));
                                existing.setResendCount(0);
                                return passwordResetRepository.save(existing)
                                        .thenReturn(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.FIELD_RESET_PASSWORD_RESEND_LIMIT, ErrorCode.FIELD_RESET_PASSWORD_RESEND_LIMIT));
                            }

                            // 4. Cleanup stale token before creating new one
                            return passwordResetRepository.delete(existing).then(Mono.empty());
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            String token = java.util.UUID.randomUUID().toString();
                            Instant expiryTime = now.plusSeconds(resetExpiryMinutes * 60L);
                            long expiryUnix = expiryTime.getEpochSecond();

                            // Construct link with token and expiry
                            String resetLink = String.format(
                                    "https://splitsmart.onewordmax.com/reset-password?token=%s&expires=%d",
                                    token, expiryUnix
                            );

                            return mailUtils.sendResetPasswordMail(email, resetLink)
                                    .flatMap(mailRes -> {
                                        PasswordResetToken newToken = new PasswordResetToken();
                                        newToken.setEmail(email);
                                        newToken.setToken(token);
                                        newToken.setExpiryTime(expiryTime);
                                        newToken.setResendCount(0);
                                        newToken.setIpAddress(ip);
                                        newToken.setUserAgent(userAgent);
                                        newToken.setUsed(false);
                                        newToken.setCreatedAt(now);
                                        return passwordResetRepository.save(newToken);
                                    })
                                    .thenReturn(SuccessResponse.of(HttpStatus.OK, SuccessCode.RESET_PASSWORD_SENT, null, null));
                        })))
                // Security: Return same response if user doesn't exist
                .switchIfEmpty(Mono.just(SuccessResponse.of(HttpStatus.OK, SuccessCode.RESET_PASSWORD_SENT, null, null)))
                .map(response -> (ResponseEntity<?>) response);
    }

    @Transactional
    public Mono<ResponseEntity<?>> verifyAndResetPassword(String token, String newPassword) {
        Instant now = Instant.now();

        return passwordResetRepository.findByToken(token)
                .flatMap(resetToken -> {
                    // Validation checks
                    if (resetToken.isUsed()) {
                        log.warn("Reset token already used: {}", token);
                        return Mono.just(ErrorResponse.of(HttpStatus.GONE, ErrorCode.FIELD_RESET_PASSWORD_ALREADY_USED, ErrorCode.FIELD_RESET_PASSWORD_ALREADY_USED));
                    }
                    if (resetToken.getExpiryTime().isBefore(now)) {
                        log.warn("Reset token expired: {}", token);
                        return Mono.just(ErrorResponse.of(HttpStatus.GONE, ErrorCode.FIELD_RESET_PASSWORD_EXPIRED, ErrorCode.FIELD_RESET_PASSWORD_EXPIRED));
                    }

                    // Proceed to update password
                    return userRepository.findByEmail(resetToken.getEmail())
                            .flatMap(user -> {
                                return Mono.fromCallable(() -> passwordEncoder.encode(newPassword))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .flatMap(hashedPassword -> {
                                            user.setPassword(hashedPassword);
                                            resetToken.setUsed(true); // Invalidate token

                                            return userRepository.save(user)
                                                    .then(passwordResetRepository.save(resetToken))
                                                    .thenReturn(SuccessResponse.of(HttpStatus.OK, SuccessCode.OTP_OK, "Password updated successfully", null));
                                        });
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Invalid reset token provided: {}", token);
                    return Mono.just(ErrorResponse.of(HttpStatus.NOT_FOUND, ErrorCode.FIELD_OTP_EMPTY, "Invalid token"));
                }))
                .map(response -> (ResponseEntity<?>) response);
    }

    /* -------------------- HELPERS -------------------- */

    /**
     * Specialized handler for Login to distinguish from Signup logic.
     */
    private Mono<ResponseEntity<?>> handleLoginStatus(User user) {
        log.info("Handling login status, userId={}, status={}", user.getId(), user.getStatus());
        return switch (user.getStatus()) {
            // TODO: have user verify their email again in the case of INACTIVE
            case ACTIVE, INACTIVE ->
                    userUtils.getInternalUserProfile(Long.valueOf(user.getUserId()), user.getRole().toString().toUpperCase())
                            .flatMap(profile -> {
                                LoginResponse loginData = new LoginResponse(
                                        user.getId(),
                                        user.getUsername(),
                                        profile
                                );

                                return Mono.<ResponseEntity<?>>just(SuccessResponse.of(
                                        HttpStatus.OK,
                                        SuccessCode.LOGIN_OK,
                                        loginData,
                                        null
                                ));
                            })
                            .onErrorResume(e -> {
                                log.error("Failed to fetch profile for user {}: {}", user.getId(), e.getMessage());
                                return Mono.<ResponseEntity<?>>just(ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.SERVICE_INTERNAL_SERVER_ERROR, ErrorCode.SERVICE_INTERNAL_SERVER_ERROR));
                            });
            case BLOCKED -> {
                log.warn("Login rejected: User blocked, userId={}", user.getId());
                yield Mono.<ResponseEntity<?>>just(ErrorResponse.of(HttpStatus.FORBIDDEN, ErrorCode.RESOURCE_USER_BLOCKED, ErrorCode.REQUEST_INVALID));
            }
            case DELETED -> {
                log.warn("Login rejected: User account deleted, userId={}", user.getId());
                yield Mono.<ResponseEntity<?>>just(ErrorResponse.of(HttpStatus.GONE, ErrorCode.RESOURCE_USER_EMPTY, ErrorCode.REQUEST_INVALID));
            }
        };
    }

    public Mono<User> getUserByEmailOrUsername(String value) {
        return userRepository.findByEmail(value)
                .switchIfEmpty(userRepository.findByUsername(value));
    }

    /* -------------------- GUEST USER -------------------- */

    public Mono<ResponseEntity<?>> generateGuestResponse() {
        return userRepository.countByUsernameStartingWith("guest")
                .map(count -> "guest" + generateHash(count + 1))
                .map(username -> SuccessResponse.of(
                        HttpStatus.OK,
                        "Guest username generated",
                        username,
                        null
                ));
    }

    private String generateHash(long base) {
        try {
            String salt = java.util.UUID.randomUUID().toString().substring(0, 4);
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((base + "-" + salt).getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString().substring(0, 6);
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }


}
