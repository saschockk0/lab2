package com.rus.bank.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateCardRequest {
    @NotNull
    private Long accountId;

    @NotNull
    private String holderName;

    private LocalDate expiryDate; // опционально, по умолчанию +3 года
}

