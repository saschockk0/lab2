package com.rus.bank.controller;

import com.rus.bank.dto.CardDto;
import com.rus.bank.dto.CreateCardRequest;
import com.rus.bank.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDto> create(@Valid @RequestBody CreateCardRequest req) {
        CardDto saved = cardService.create(req);
        return ResponseEntity
                .created(URI.create("/api/cards/" + saved.getId()))
                .body(saved);
    }

    @GetMapping
    public List<CardDto> getAll() {
        return cardService.getAll();
    }

    @GetMapping("/{id}")
    public CardDto getById(@PathVariable Long id) {
        return cardService.getById(id);
    }

    @GetMapping("/account/{accountId}")
    public List<CardDto> getByAccountId(@PathVariable Long accountId) {
        return cardService.getByAccountId(accountId);
    }

    @PatchMapping("/{id}/block")
    public CardDto blockCard(@PathVariable Long id) {
        return cardService.blockCard(id);
    }

    @PatchMapping("/{id}/unblock")
    public CardDto unblockCard(@PathVariable Long id) {
        return cardService.unblockCard(id);
    }
}

