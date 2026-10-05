package com.onewordmax.splitsmart.gateway.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {

    @Id
    private Long id;

    @Column("username")
    private String username;

    @Column("user_id")
    private String userId;

    @Column("password")
    private String password;

    @Column("email")
    private String email;

    @Column("authentication_method")
    private AuthenticationMethod authenticationMethod;

    @Column("status")
    private Status status;

    @Column("role")
    private Roles role;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt = Instant.now(); // stored in UTC

    public enum AuthenticationMethod {
        CREDENTIALS, GOOGLE
    }

    public enum Status {
        ACTIVE, INACTIVE, BLOCKED, DELETED
    }

    public enum Roles {
        GUEST, USER
    }
}
