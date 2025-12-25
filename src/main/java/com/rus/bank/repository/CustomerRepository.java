package com.rus.bank.repository;

import com.rus.bank.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByTaxId(String taxId);
    Optional<Customer> findByEmail(String email);
    boolean existsByTaxId(String taxId);
    boolean existsByEmail(String email);
}

