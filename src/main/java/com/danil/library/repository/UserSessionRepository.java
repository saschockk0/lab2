package com.danil.library.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findByRefreshJti(String refreshJti);

    // на шаг 5 теста: можно одним методом пометить просроченные как EXPIRED (по желанию)
    long deleteByExpiresAtBefore(Instant now);
}
