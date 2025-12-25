package com.rus.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards",
       uniqueConstraints = @UniqueConstraint(columnNames = "card_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number", nullable = false, unique = true, length = 16)
    private String cardNumber; // маска: XXXX XXXX XXXX XXXX

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false, length = 3)
    private String cvv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (cardNumber == null) {
            cardNumber = generateCardNumber();
        }
        if (cvv == null) {
            cvv = generateCvv();
        }
        if (expiryDate == null) {
            // По умолчанию карта действует 3 года
            expiryDate = LocalDate.now().plusYears(3);
        }
    }

    private String generateCardNumber() {
        // Упрощенная генерация (в реальности используется алгоритм Luhn)
        long random = System.currentTimeMillis() % 10000000000000000L;
        return String.format("%016d", random);
    }

    private String generateCvv() {
        return String.format("%03d", (int)(Math.random() * 1000));
    }

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public boolean isActive() {
        return status == CardStatus.ACTIVE && !isExpired();
    }
}

