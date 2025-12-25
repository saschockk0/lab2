package com.rus.bank.controller;

import com.rus.bank.dto.LoginRequest;
import com.rus.bank.dto.RefreshRequest;
import com.rus.bank.dto.RegisterRequest;
import com.rus.bank.dto.TokenPairResponse;
import com.rus.bank.security.AuthService;
import com.rus.bank.service.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserAccountService userAccountService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequest req) {
        userAccountService.register(req);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public TokenPairResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public TokenPairResponse refresh(@RequestBody RefreshRequest req) {
        return authService.refresh(req);
    }
}

