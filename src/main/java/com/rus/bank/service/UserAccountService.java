package com.rus.bank.service;

import com.rus.bank.dto.RegisterRequest;
import com.rus.bank.model.UserAccount;
import com.rus.bank.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final UserAccountRepository repo;
    private final PasswordEncoder encoder;

    public void register(RegisterRequest req) {
        String username = req.getUsername();
        String password = req.getPassword();

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password is required");
        }
        if (repo.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        validatePassword(password);

        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));

        // Используем роль из запроса, по умолчанию CLIENT
        String role = (req.getRole() != null && !req.getRole().isBlank())
                ? req.getRole().toUpperCase() // Приводим к верхнему регистру для консистентности
                : "CLIENT";
        user.setRole(role);

        repo.save(user);
    }

    /**
     * Валидация пароля: минимум 8 символов, хотя бы одна буква и хотя бы одна цифра
     */
    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }

        if (!hasLetter) {
            throw new IllegalArgumentException("Password must contain at least one letter");
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }
}

