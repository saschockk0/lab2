package com.rus.bank.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateAccountRequest {
    @NotNull
    private Long customerId;

    private String currency = "RUB";

    @NotNull
    private String type; // "DEBIT" or "CREDIT"

    @Positive
    private BigDecimal creditLimit; // только для CREDIT
}

