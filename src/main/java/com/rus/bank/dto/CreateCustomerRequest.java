package com.rus.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCustomerRequest {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Pattern(regexp = "\\d{10}|\\d{12}", message = "Tax ID must be 10 or 12 digits")
    private String taxId; // ИНН: 10 или 12 цифр

    @NotBlank
    @Email
    private String email;

    private String phone;

    private String address;
}

