package com.danil.library.security;

import com.danil.library.dto.LoginRequest;
import com.danil.library.dto.RefreshRequest;
import com.danil.library.dto.TokenPairResponse;
import com.danil.library.model.UserAccount;
import com.danil.library.repository.UserAccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;
    private final UserSessionRepository sessions;

    public AuthService(UserAccountRepository users,
                       PasswordEncoder encoder,
                       JwtTokenProvider jwt,
                       UserSessionRepository sessions) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.sessions = sessions;
    }

    @Transactional
    public TokenPairResponse login(LoginRequest req) {
        // 1) проверяю логин/пароль
        UserAccount user = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Bad credentials"));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Bad credentials");
        }

        // 2) генерю access + refresh
        String access = jwt.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        JwtTokenProvider.RefreshTokenData refresh = jwt.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

        // 3) сохраняю refresh-сессию в БД
        sessions.save(new UserSession(user.getId(), refresh.jti(), refresh.expiresAt()));

        return new TokenPairResponse(access, refresh.token());
    }

    @Transactional
    public TokenPairResponse refresh(RefreshRequest req) {
        String refreshToken = req.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken is required");
        }

        // 1) валидирую JWT refresh
        Jws<Claims> jws = jwt.parseAndValidate(refreshToken);
        Claims claims = jws.getBody();
        jwt.assertTokenType(claims, "refresh");

        String oldJti = claims.getId();
        Long userId = claims.get("uid", Long.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        // 2) проверяю, что такой refresh вообще “живой” в таблице user_sessions
        UserSession session = sessions.findByRefreshJti(oldJti)
                .orElseThrow(() -> new IllegalArgumentException("Refresh session not found"));

        // 3) проверка статуса + expiry
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Refresh token already used/revoked");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            throw new IllegalArgumentException("Refresh token expired");
        }

        // 4) РОТАЦИЯ: старый refresh помечаю ROTATED и выдаю новый
        session.setStatus(SessionStatus.ROTATED);
        session.setRotatedAt(Instant.now());
        sessions.save(session);

        // 5) создаю новую пару
        String newAccess = jwt.generateAccessToken(userId, username, role);
        JwtTokenProvider.RefreshTokenData newRefresh = jwt.generateRefreshToken(userId, username, role);

        sessions.save(new UserSession(userId, newRefresh.jti(), newRefresh.expiresAt()));

        return new TokenPairResponse(newAccess, newRefresh.token());
    }
}
