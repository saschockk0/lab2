package com.danil.library.controller;

import com.danil.library.dto.LoginRequest;
import com.danil.library.dto.RefreshRequest;
import com.danil.library.dto.RegisterRequest;
import com.danil.library.dto.TokenPairResponse;
import com.danil.library.security.AuthService;
import com.danil.library.service.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountService userAccountService;
    private final AuthService authService;

    public AuthController(UserAccountService userAccountService, AuthService authService) {
        this.userAccountService = userAccountService;
        this.authService = authService;
    }

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
