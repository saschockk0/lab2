package com.rus.bank.repository;

import com.rus.bank.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);
    List<Card> findByAccountId(Long accountId);
    List<Card> findByAccountIdAndStatus(Long accountId, Card.CardStatus status);
    boolean existsByCardNumber(String cardNumber);
}

