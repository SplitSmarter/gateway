package com.onewordmax.splitsmart.gateway.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("signup_otp")
public class SignupOtp {

    @Id
    private Long id;

    @Column("email")
    private String email;

    @Column("otp_code")
    private String otpCode;

    @Column("status")
    private OtpStatus status;

    @Column("exp_time")
    private Instant expiryTime;

    @Column("attempts")
    private int attempts = 0;

    @Column("resend_count")
    private int resendCount = 0;

    // the time until a new otp code can be requested
    @Column("lock_until")
    private Instant lockUntil;

    @Column("verified_at")
    private Instant verifiedAt;

    @Column("ip_addr")
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;

    @Column("last_sent_at")
    private Instant lastSentAt;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt = Instant.now();

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt = Instant.now();

    public enum OtpStatus {
        PENDING, VERIFIED, LOCKED, USED
    }
}
