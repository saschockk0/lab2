package com.rus.bank.service;

import com.rus.bank.dto.CardDto;
import com.rus.bank.dto.CreateCardRequest;
import com.rus.bank.exception.NotFoundException;
import com.rus.bank.model.Account;
import com.rus.bank.model.Card;
import com.rus.bank.repository.AccountRepository;
import com.rus.bank.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public CardDto create(CreateCardRequest req) {
        log.info("Creating card for account id: {}", req.getAccountId());
        
        Account account = accountRepository.findById(req.getAccountId())
                .orElseThrow(() -> new NotFoundException("Account with id=" + req.getAccountId() + " not found"));

        if (account.getStatus() != Account.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Cannot create card for non-active account");
        }

        Card card = new Card();
        card.setAccount(account);
        card.setHolderName(req.getHolderName());
        card.setExpiryDate(req.getExpiryDate() != null ? req.getExpiryDate() : LocalDate.now().plusYears(3));
        card.setStatus(Card.CardStatus.ACTIVE);

        card = cardRepository.save(card);
        log.info("Created card with number: {} for account id: {}", maskCardNumber(card.getCardNumber()), account.getId());
        return toDto(card);
    }

    @Transactional(readOnly = true)
    public List<CardDto> getAll() {
        return cardRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CardDto getById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card with id=" + id + " not found"));
        return toDto(card);
    }

    @Transactional(readOnly = true)
    public List<CardDto> getByAccountId(Long accountId) {
        return cardRepository.findByAccountId(accountId).stream().map(this::toDto).toList();
    }

    @Transactional
    public CardDto blockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card with id=" + id + " not found"));
        card.setStatus(Card.CardStatus.BLOCKED);
        card = cardRepository.save(card);
        log.info("Blocked card with id: {}", id);
        return toDto(card);
    }

    @Transactional
    public CardDto unblockCard(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card with id=" + id + " not found"));
        card.setStatus(Card.CardStatus.ACTIVE);
        card = cardRepository.save(card);
        log.info("Unblocked card with id: {}", id);
        return toDto(card);
    }

    private CardDto toDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());
        dto.setCardNumber(maskCardNumber(card.getCardNumber()));
        dto.setAccountId(card.getAccount() != null ? card.getAccount().getId() : null);
        dto.setAccountNumber(card.getAccount() != null ? card.getAccount().getAccountNumber() : null);
        dto.setHolderName(card.getHolderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus().name());
        dto.setCreatedAt(card.getCreatedAt());
        return dto;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return cardNumber;
        }
        // Маскируем все цифры кроме последних 4
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}

