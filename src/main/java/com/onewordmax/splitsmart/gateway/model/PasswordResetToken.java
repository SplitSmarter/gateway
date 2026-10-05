package com.onewordmax.splitsmart.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("password_reset_tokens")
public class PasswordResetToken {
    @Id
    private Long id;
    private String email;
    private String token;
    private Instant expiryTime;
    private Instant lockUntil;
    private int resendCount;
    private String ipAddress;
    private String userAgent;
    private boolean used;
    private Instant createdAt;
}