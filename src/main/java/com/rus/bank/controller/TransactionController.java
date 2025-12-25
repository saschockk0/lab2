package com.rus.bank.controller;

import com.rus.bank.dto.CreateTransactionRequest;
import com.rus.bank.dto.TransactionDto;
import com.rus.bank.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * КРИТИЧЕСКАЯ ОПЕРАЦИЯ: Перевод денег между счетами
     * Использует @Transactional для обеспечения атомарности
     */
    @PostMapping
    public ResponseEntity<TransactionDto> transfer(@Valid @RequestBody CreateTransactionRequest req) {
        TransactionDto saved = transactionService.transfer(req);
        return ResponseEntity
                .created(URI.create("/api/transactions/" + saved.getId()))
                .body(saved);
    }

    @GetMapping
    public List<TransactionDto> getAll() {
        return transactionService.getAll();
    }

    @GetMapping("/page")
    public Page<TransactionDto> getPage(Pageable pageable) {
        return transactionService.getPage(pageable);
    }

    @GetMapping("/{id}")
    public TransactionDto getById(@PathVariable Long id) {
        return transactionService.getById(id);
    }

    @GetMapping("/account/{accountId}")
    public List<TransactionDto> getByAccountId(@PathVariable Long accountId) {
        return transactionService.getByAccountId(accountId);
    }

    @GetMapping("/failed")
    public List<TransactionDto> getFailedTransactions() {
        return transactionService.getFailedTransactions();
    }
}

