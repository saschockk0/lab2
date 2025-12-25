package com.rus.bank.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerRequest {
    private String firstName;
    private String lastName;
    
    @Email
    private String email;
    
    private String phone;
    private String address;
}

