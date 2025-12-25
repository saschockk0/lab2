package com.rus.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts",
       uniqueConstraints = @UniqueConstraint(columnNames = "account_number"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber; // генерируется автоматически

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "RUB"; // ISO 4217

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type = AccountType.DEBIT;

    @Column(name = "credit_limit", precision = 19, scale = 2)
    private BigDecimal creditLimit; // для CREDIT счетов

    @Column(name = "opened_date", nullable = false, updatable = false)
    private LocalDateTime openedDate;

    @PrePersist
    protected void onCreate() {
        if (openedDate == null) {
            openedDate = LocalDateTime.now();
        }
        if (accountNumber == null) {
            accountNumber = generateAccountNumber();
        }
        if (creditLimit == null && type == AccountType.CREDIT) {
            creditLimit = BigDecimal.ZERO;
        }
    }

    private String generateAccountNumber() {
        // Простая генерация: 40702 + случайные цифры
        long random = System.currentTimeMillis() % 1000000000000000L;
        return "40702" + String.format("%015d", random);
    }

    public enum AccountStatus {
        ACTIVE, BLOCKED, CLOSED
    }

    public enum AccountType {
        DEBIT, CREDIT
    }

    /**
     * Проверка возможности списания для дебетового счета
     * Дебетовый счет НИКОГДА не может уйти в минус
     * Кредитный счет может уйти в минус до лимита
     */
    public boolean canWithdraw(BigDecimal amount) {
        if (status != AccountStatus.ACTIVE) {
            return false;
        }
        if (type == AccountType.DEBIT) {
            // Для дебетового счета баланс должен быть >= суммы списания
            return balance.compareTo(amount) >= 0;
        } else { // CREDIT
            // Для кредитного счета: баланс + лимит >= суммы списания
            BigDecimal available = balance.add(creditLimit != null ? creditLimit : BigDecimal.ZERO);
            return available.compareTo(amount) >= 0;
        }
    }

    /**
     * Проверка достаточности средств с учетом типа счета
     */
    public BigDecimal getAvailableBalance() {
        if (type == AccountType.DEBIT) {
            return balance;
        } else { // CREDIT
            return balance.add(creditLimit != null ? creditLimit : BigDecimal.ZERO);
        }
    }
}

