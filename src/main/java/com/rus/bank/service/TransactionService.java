package com.rus.bank.service;

import com.rus.bank.dto.CreateTransactionRequest;
import com.rus.bank.dto.TransactionDto;
import com.rus.bank.exception.NotFoundException;
import com.rus.bank.model.Account;
import com.rus.bank.model.Transaction;
import com.rus.bank.repository.AccountRepository;
import com.rus.bank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * КРИТИЧЕСКАЯ БИЗНЕС-ОПЕРАЦИЯ: Перевод денег между счетами
     * Использует @Transactional для обеспечения атомарности и консистентности
     */
    @Transactional
    public TransactionDto transfer(CreateTransactionRequest req) {
        log.info("Processing transfer: {} {} from account {} to account {}",
                req.getAmount(), req.getCurrency(), req.getFromAccountId(), req.getToAccountId());

        // 1. Получаем счета с блокировкой для предотвращения concurrent updates
        Account fromAccount = accountRepository.findById(req.getFromAccountId())
                .orElseThrow(() -> new NotFoundException("From account with id=" + req.getFromAccountId() + " not found"));
        
        Account toAccount = accountRepository.findById(req.getToAccountId())
                .orElseThrow(() -> new NotFoundException("To account with id=" + req.getToAccountId() + " not found"));

        // 2. Проверяем, что оба счета активны
        if (fromAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            Transaction failed = createFailedTransaction(fromAccount, toAccount, req, 
                    "From account is not active: " + fromAccount.getStatus());
            log.warn("Transfer failed: from account {} is not active", req.getFromAccountId());
            return toDto(failed);
        }

        if (toAccount.getStatus() != Account.AccountStatus.ACTIVE) {
            Transaction failed = createFailedTransaction(fromAccount, toAccount, req, 
                    "To account is not active: " + toAccount.getStatus());
            log.warn("Transfer failed: to account {} is not active", req.getToAccountId());
            return toDto(failed);
        }

        // 3. Проверяем валюту
        if (!fromAccount.getCurrency().equals(req.getCurrency()) || 
            !toAccount.getCurrency().equals(req.getCurrency())) {
            Transaction failed = createFailedTransaction(fromAccount, toAccount, req, 
                    "Currency mismatch");
            log.warn("Transfer failed: currency mismatch");
            return toDto(failed);
        }

        // 4. КРИТИЧЕСКАЯ ПРОВЕРКА: Достаточность средств на счете-отправителе
        // Для дебетового счета баланс НИКОГДА не может уйти в минус
        if (!fromAccount.canWithdraw(req.getAmount())) {
            Transaction failed = createFailedTransaction(fromAccount, toAccount, req, 
                    "Insufficient funds. Available balance: " + fromAccount.getAvailableBalance());
            log.warn("Transfer failed: insufficient funds on account {}. Requested: {}, Available: {}", 
                    req.getFromAccountId(), req.getAmount(), fromAccount.getAvailableBalance());
            return toDto(failed);
        }

        // 5. Выполняем перевод (все изменения происходят в одной транзакции БД)
        fromAccount.setBalance(fromAccount.getBalance().subtract(req.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(req.getAmount()));

        // Сохраняем обновленные балансы
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 6. Создаем запись о транзакции со статусом COMPLETED
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(req.getAmount());
        transaction.setCurrency(req.getCurrency());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setDescription(req.getDescription());

        transaction = transactionRepository.save(transaction);
        log.info("Transfer completed: transaction id={}, amount={} {} from account {} to account {}", 
                transaction.getId(), req.getAmount(), req.getCurrency(), req.getFromAccountId(), req.getToAccountId());
        
        return toDto(transaction);
    }

    private Transaction createFailedTransaction(Account fromAccount, Account toAccount, 
                                               CreateTransactionRequest req, String failureReason) {
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(req.getAmount());
        transaction.setCurrency(req.getCurrency());
        transaction.setStatus(Transaction.TransactionStatus.FAILED);
        transaction.setDescription(req.getDescription());
        transaction.setFailureReason(failureReason);
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getAll() {
        return transactionRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> getPage(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public TransactionDto getById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction with id=" + id + " not found"));
        return toDto(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getByAccountId(Long accountId) {
        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getFailedTransactions() {
        return transactionRepository.findByStatus(Transaction.TransactionStatus.FAILED)
                .stream().map(this::toDto).toList();
    }

    private TransactionDto toDto(Transaction transaction) {
        TransactionDto dto = new TransactionDto();
        dto.setId(transaction.getId());
        dto.setFromAccountId(transaction.getFromAccount() != null ? transaction.getFromAccount().getId() : null);
        dto.setFromAccountNumber(transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null);
        dto.setToAccountId(transaction.getToAccount() != null ? transaction.getToAccount().getId() : null);
        dto.setToAccountNumber(transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null);
        dto.setAmount(transaction.getAmount());
        dto.setCurrency(transaction.getCurrency());
        dto.setTimestamp(transaction.getTimestamp());
        dto.setStatus(transaction.getStatus().name());
        dto.setDescription(transaction.getDescription());
        dto.setFailureReason(transaction.getFailureReason());
        return dto;
    }
}

