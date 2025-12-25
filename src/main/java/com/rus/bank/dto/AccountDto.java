package com.rus.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private String customerName;
    private BigDecimal balance;
    private String currency;
    private String status;
    private String type;
    private BigDecimal creditLimit;
    private LocalDateTime openedDate;
}

