package com.rus.bank.security;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "user_sessions",
       indexes = {
               @Index(name = "ix_user_sessions_user", columnList = "user_id"),
               @Index(name = "ix_user_sessions_refresh_jti", columnList = "refresh_jti", unique = true)
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "refresh_jti", nullable = false, unique = true, length = 80)
    private String refreshJti;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "rotated_at")
    private Instant rotatedAt;

    public UserSession(Long userId, String refreshJti, Instant expiresAt) {
        this.userId = userId;
        this.refreshJti = refreshJti;
        this.expiresAt = expiresAt;
        this.status = SessionStatus.ACTIVE;
        this.createdAt = Instant.now();
    }
}

