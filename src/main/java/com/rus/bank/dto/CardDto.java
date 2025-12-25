package com.rus.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDto {
    private Long id;
    private String cardNumber; // маскированный: **** **** **** 1234
    private Long accountId;
    private String accountNumber;
    private String holderName;
    private LocalDate expiryDate;
    private String status;
    private LocalDateTime createdAt;
}

