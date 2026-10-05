package com.onewordmax.splitsmart.gateway.service;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.SuccessCode;
import com.onewordmax.splitsmart.gateway.dto.ErrorResponse;
import com.onewordmax.splitsmart.gateway.dto.SuccessResponse;
import com.onewordmax.splitsmart.gateway.model.SignupOtp;
import com.onewordmax.splitsmart.gateway.repositories.OtpRepository;
import com.onewordmax.splitsmart.gateway.repositories.UserRepository;
import com.onewordmax.splitsmart.gateway.utils.MailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final MailUtils mailUtils;
    private final UserRepository userRepository;

    @Value("${otp.expiration.minutes}")
    private int otpExpiryMinutes;
    @Value("${otp.max.attempts}")
    private int maxAttempts;
    @Value("${otp.resend.limit}")
    private int resendLimit;
    @Value("${otp.resend.lock.seconds}")
    private int resendLockSeconds;
    @Value("${otp.reverification.seconds}")
    private int reverificationSeconds;

    /* -------------------- SEND OTP -------------------- */

    public Mono<ResponseEntity<?>> sendOtp(String email, String ip, String userAgent) {
        Instant now = Instant.now();

        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("User with this email already exists use resend otp instead, email={}", email);
                        return Mono.just(ErrorResponse.of(HttpStatus.CONFLICT, ErrorCode.RESOURCE_USER_CONFLICT, ErrorCode.RESOURCE_USER_CONFLICT));
                    }
                    return otpRepository.findByEmail(email)
                            .flatMap(existingOtp -> {
                                if (existingOtp.getLockUntil() != null && existingOtp.getLockUntil().isAfter(now)) {
                                    log.warn("Option to send OTP is locked, email={}", email);
                                    return Mono.just(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.FIELD_OTP_LOCKED, ErrorCode.FIELD_OTP_LOCKED));
                                }
                                if (existingOtp.getExpiryTime().isAfter(now)) {
                                    log.warn("Option to send OTP is already sent and not expired, email={}", email);
                                    return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST, ErrorCode.FIELD_OTP_ALREADY_SENT, ErrorCode.FIELD_OTP_ALREADY_SENT));
                                }
                                if (existingOtp.getStatus() == SignupOtp.OtpStatus.VERIFIED || existingOtp.getStatus() == SignupOtp.OtpStatus.USED) {
                                    if (!existingOtp.getVerifiedAt().isBefore(now.plusSeconds(reverificationSeconds))) {
                                        log.warn("Option to send OTP is verified, email={}", email);
                                        return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST, ErrorCode.FIELD_OTP_VERIFIED, ErrorCode.FIELD_OTP_VERIFIED));
                                    }
                                }
                                log.debug("Deleting stale/expired OTP record. Current time={}, db_lock_time={}", now, existingOtp.getLockUntil());
                                return otpRepository.delete(existingOtp).then(Mono.empty());
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                String otpCode = String.valueOf(100000 + new Random().nextInt(900000));
                                return mailUtils.sendSignupOtp(email, otpCode)
                                        .flatMap(mailResponse -> otpRepository.save(createOtpEntity(email, otpCode, ip, userAgent, now)))
                                        .thenReturn(SuccessResponse.of(HttpStatus.OK, SuccessCode.OTP_SENT, null, null));
                            }));
                })
                .map(response -> (ResponseEntity<?>) response);
    }

    /* -------------------- VERIFY OTP -------------------- */

    public Mono<ResponseEntity<?>> verifyOtp(String email, String otpCode, String ip, String userAgent) {
        Instant now = Instant.now();

        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("User {} already fully registered, email={}", email, email);
                        return Mono.just(ErrorResponse.of(HttpStatus.CONFLICT, ErrorCode.RESOURCE_USER_CONFLICT, ErrorCode.RESOURCE_USER_CONFLICT));
                    }
                    return otpRepository.findByEmail(email)
                            .flatMap(otp -> {
                                log.debug("Existing otp found, otp_status={}", otp.getStatus());
                                if (!Objects.equals(otp.getUserAgent(), userAgent)) {
                                    log.debug("Security context mismatch (UserAgent changed), email={}", email);
                                    return Mono.just(ErrorResponse.of(HttpStatus.BAD_REQUEST, ErrorCode.FIELD_OTP_CONTEXT_CHANGED, ErrorCode.FIELD_OTP_CONTEXT_CHANGED));
                                }
                                if (otp.getExpiryTime().isBefore(now)) {
                                    log.warn("OTP expired at {}, email={}", otp.getExpiryTime(), email);
                                    return Mono.just(ErrorResponse.of(HttpStatus.GONE, ErrorCode.FIELD_OTP_EXPIRED, ErrorCode.FIELD_OTP_EXPIRED));
                                }
                                if (!otp.getOtpCode().equals(otpCode)) {
                                    log.warn("Invalid OTP code entered, email={}", email);
                                    return handleInvalidAttempt(otp);
                                }

                                otp.setStatus(SignupOtp.OtpStatus.VERIFIED);
                                otp.setVerifiedAt(now);
                                return otpRepository.save(otp)
                                        .thenReturn(SuccessResponse.of(HttpStatus.OK, SuccessCode.OTP_OK, null, null));
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                log.warn("No pending OTP found, email={}", email);
                                return Mono.just(ErrorResponse.of(HttpStatus.NOT_FOUND, ErrorCode.FIELD_OTP_EMPTY, ErrorCode.FIELD_OTP_EMPTY));
                            }));
                })
                .map(response -> (ResponseEntity<?>) response);
    }

    /* -------------------- RESEND OTP -------------------- */

    public Mono<ResponseEntity<?>> resendOtp(String email, String ip, String userAgent) {
        Instant now = Instant.now();

        return otpRepository.findByEmail(email)
                .flatMap(otp -> {
                    if (otp.getLockUntil() != null && otp.getLockUntil().isAfter(now)) {
                        log.warn("Account {} is currently locked until {}, email={}", email, otp.getLockUntil(), email);
                        return Mono.just(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.FIELD_OTP_LOCKED, ErrorCode.FIELD_OTP_LOCKED));
                    }

                    if (otp.getResendCount() >= resendLimit) {
                        log.warn("Max resend limit reached Locking account, email={}", email);
                        otp.setLockUntil(now.plusSeconds(resendLockSeconds));
                        otp.setResendCount(0);
                        return otpRepository.save(otp)
                                .thenReturn(ErrorResponse.of(HttpStatus.TOO_MANY_REQUESTS, ErrorCode.FIELD_OTP_RESEND_LIMIT, ErrorCode.FIELD_OTP_RESEND_LIMIT));
                    }

                    String newOtpCode = String.valueOf(100000 + new Random().nextInt(900000));
                    return mailUtils.sendSignupOtp(email, newOtpCode)
                            .flatMap(res -> {
                                otp.setOtpCode(newOtpCode);
                                otp.setExpiryTime(now.plusSeconds(otpExpiryMinutes * 60L));
                                otp.setLastSentAt(now);
                                otp.setIpAddress(ip);
                                otp.setUserAgent(userAgent);
                                otp.setAttempts(0);
                                otp.setResendCount(otp.getResendCount() + 1);
                                return otpRepository.save(otp);
                            })
                            .thenReturn(SuccessResponse.of(HttpStatus.OK, SuccessCode.OTP_SENT, null, null));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Resend failed: No existing OTP found for email {}", email);
                    return Mono.just(ErrorResponse.of(HttpStatus.NOT_FOUND, ErrorCode.FIELD_OTP_EMPTY, ErrorCode.FIELD_OTP_EMPTY));
                }))
                .map(response -> (ResponseEntity<?>) response);
    }

    /* -------------------- HELPERS -------------------- */

    private Mono<ResponseEntity<?>> handleInvalidAttempt(SignupOtp otp) {
        otp.setAttempts(otp.getAttempts() + 1);
        if (otp.getAttempts() >= maxAttempts) {
            log.warn("Max OTP attempts reached for {}. Deleting OTP record.", otp.getEmail());
            return otpRepository.delete(otp)
                    .thenReturn(ErrorResponse.of(HttpStatus.FORBIDDEN, ErrorCode.FIELD_OTP_ATTEMPTS_REACHED, ErrorCode.FIELD_OTP_ATTEMPTS_REACHED));
        }
        return otpRepository.save(otp)
                .thenReturn(ErrorResponse.of(HttpStatus.UNAUTHORIZED, ErrorCode.FIELD_OTP_INVALID, ErrorCode.FIELD_OTP_INVALID));
    }

    private SignupOtp createOtpEntity(String email, String code, String ip, String ua, Instant now) {
        SignupOtp otp = new SignupOtp();
        otp.setEmail(email);
        otp.setOtpCode(code);
        otp.setStatus(SignupOtp.OtpStatus.PENDING);
        otp.setExpiryTime(now.plusSeconds(otpExpiryMinutes * 60L));
        otp.setLastSentAt(now);
        otp.setAttempts(0);
        otp.setResendCount(0);
        otp.setIpAddress(ip);
        otp.setUserAgent(ua);
        return otp;
    }
}