package com.rus.bank.security;

import com.rus.bank.dto.LoginRequest;
import com.rus.bank.dto.RefreshRequest;
import com.rus.bank.dto.TokenPairResponse;
import com.rus.bank.model.UserAccount;
import com.rus.bank.repository.UserAccountRepository;
import com.rus.bank.repository.UserSessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;
    private final UserSessionRepository sessions;

    @Transactional
    public TokenPairResponse login(LoginRequest req) {
        UserAccount user = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Bad credentials"));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Bad credentials");
        }

        String access = jwt.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        JwtTokenProvider.RefreshTokenData refresh = jwt.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());

        sessions.save(new UserSession(user.getId(), refresh.jti(), refresh.expiresAt()));

        return new TokenPairResponse(access, refresh.token());
    }

    @Transactional
    public TokenPairResponse refresh(RefreshRequest req) {
        String refreshToken = req.getRefreshToken();
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken is required");
        }

        Jws<Claims> jws = jwt.parseAndValidate(refreshToken);
        Claims claims = jws.getBody();
        jwt.assertTokenType(claims, "refresh");

        String oldJti = claims.getId();
        Long userId = claims.get("uid", Long.class);
        String username = claims.getSubject();
        String role = claims.get("role", String.class);

        UserSession session = sessions.findByRefreshJti(oldJti)
                .orElseThrow(() -> new IllegalArgumentException("Refresh session not found"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new IllegalArgumentException("Refresh token already used/revoked");
        }
        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(SessionStatus.EXPIRED);
            sessions.save(session);
            throw new IllegalArgumentException("Refresh token expired");
        }

        session.setStatus(SessionStatus.ROTATED);
        session.setRotatedAt(Instant.now());
        sessions.save(session);

        String newAccess = jwt.generateAccessToken(userId, username, role);
        JwtTokenProvider.RefreshTokenData newRefresh = jwt.generateRefreshToken(userId, username, role);

        sessions.save(new UserSession(userId, newRefresh.jti(), newRefresh.expiresAt()));

        return new TokenPairResponse(newAccess, newRefresh.token());
    }
}

