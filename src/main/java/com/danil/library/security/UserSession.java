package com.danil.library.security;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_sessions",
        indexes = {
                @Index(name = "ix_user_sessions_user", columnList = "user_id"),
                @Index(name = "ix_user_sessions_refresh_jti", columnList = "refresh_jti", unique = true)
        })
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // я храню ссылку на пользователя просто как user_id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // jti refresh-токена — ключ к сессии
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

    public UserSession() {}

    public UserSession(Long userId, String refreshJti, Instant expiresAt) {
        this.userId = userId;
        this.refreshJti = refreshJti;
        this.expiresAt = expiresAt;
        this.status = SessionStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRefreshJti() { return refreshJti; }
    public void setRefreshJti(String refreshJti) { this.refreshJti = refreshJti; }
    public SessionStatus getStatus() { return status; }
    public void setStatus(SessionStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getRotatedAt() { return rotatedAt; }
    public void setRotatedAt(Instant rotatedAt) { this.rotatedAt = rotatedAt; }
}
