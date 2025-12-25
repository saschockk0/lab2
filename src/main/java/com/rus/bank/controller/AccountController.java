package com.rus.bank.controller;

import com.rus.bank.dto.AccountDto;
import com.rus.bank.dto.CreateAccountRequest;
import com.rus.bank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDto> create(@Valid @RequestBody CreateAccountRequest req) {
        AccountDto saved = accountService.create(req);
        return ResponseEntity
                .created(URI.create("/api/accounts/" + saved.getId()))
                .body(saved);
    }

    @GetMapping
    public List<AccountDto> getAll() {
        return accountService.getAll();
    }

    @GetMapping("/page")
    public Page<AccountDto> getPage(Pageable pageable) {
        return accountService.getPage(pageable);
    }

    @GetMapping("/{id}")
    public AccountDto getById(@PathVariable Long id) {
        return accountService.getById(id);
    }

    @GetMapping("/number/{accountNumber}")
    public AccountDto getByAccountNumber(@PathVariable String accountNumber) {
        return accountService.getByAccountNumber(accountNumber);
    }

    @GetMapping("/customer/{customerId}")
    public List<AccountDto> getByCustomerId(@PathVariable Long customerId) {
        return accountService.getByCustomerId(customerId);
    }

    @PatchMapping("/{id}/block")
    public AccountDto blockAccount(@PathVariable Long id) {
        return accountService.blockAccount(id);
    }

    @PatchMapping("/{id}/unblock")
    public AccountDto unblockAccount(@PathVariable Long id) {
        return accountService.unblockAccount(id);
    }
}

