package com.rus.bank.service;

import com.rus.bank.dto.AccountDto;
import com.rus.bank.dto.CreateAccountRequest;
import com.rus.bank.exception.NotFoundException;
import com.rus.bank.model.Account;
import com.rus.bank.model.Customer;
import com.rus.bank.repository.AccountRepository;
import com.rus.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public AccountDto create(CreateAccountRequest req) {
        log.info("Creating account for customer id: {}, type: {}", req.getCustomerId(), req.getType());
        
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer with id=" + req.getCustomerId() + " not found"));

        Account.AccountType type = Account.AccountType.valueOf(req.getType().toUpperCase());
        
        Account account = new Account();
        account.setCustomer(customer);
        account.setCurrency(req.getCurrency() != null ? req.getCurrency() : "RUB");
        account.setType(type);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.ACTIVE);
        
        if (type == Account.AccountType.CREDIT) {
            if (req.getCreditLimit() == null || req.getCreditLimit().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Credit limit must be positive for CREDIT accounts");
            }
            account.setCreditLimit(req.getCreditLimit());
        }

        account = accountRepository.save(account);
        log.info("Created account with number: {} for customer id: {}", account.getAccountNumber(), customer.getId());
        return toDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getAll() {
        return accountRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getPage(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public AccountDto getById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account with id=" + id + " not found"));
        return toDto(account);
    }

    @Transactional(readOnly = true)
    public AccountDto getByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Account with number=" + accountNumber + " not found"));
        return toDto(account);
    }

    @Transactional(readOnly = true)
    public List<AccountDto> getByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
    }

    @Transactional
    public AccountDto blockAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account with id=" + id + " not found"));
        account.setStatus(Account.AccountStatus.BLOCKED);
        account = accountRepository.save(account);
        log.info("Blocked account with id: {}", id);
        return toDto(account);
    }

    @Transactional
    public AccountDto unblockAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Account with id=" + id + " not found"));
        account.setStatus(Account.AccountStatus.ACTIVE);
        account = accountRepository.save(account);
        log.info("Unblocked account with id: {}", id);
        return toDto(account);
    }

    private AccountDto toDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setId(account.getId());
        dto.setAccountNumber(account.getAccountNumber());
        dto.setCustomerId(account.getCustomer() != null ? account.getCustomer().getId() : null);
        dto.setCustomerName(account.getCustomer() != null 
                ? account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName() 
                : null);
        dto.setBalance(account.getBalance());
        dto.setCurrency(account.getCurrency());
        dto.setStatus(account.getStatus().name());
        dto.setType(account.getType().name());
        dto.setCreditLimit(account.getCreditLimit());
        dto.setOpenedDate(account.getOpenedDate());
        return dto;
    }
}

