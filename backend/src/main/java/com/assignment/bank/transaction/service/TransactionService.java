package com.assignment.bank.transaction.service;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.repository.AccountRepository;
import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.mapper.TransactionMapper;
import com.assignment.bank.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository, TransactionMapper transactionMapper) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public TransactionResponse credit(TransactionRequest transactionRequest) {

        Account account = creditAccount(transactionRequest.IBAN(), transactionRequest.amount());
        Transaction transaction = transactionMapper.mapToEntity(transactionRequest, account);
        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.mapToResponse(savedTransaction);
    }

    private @NonNull Account creditAccount(String IBAN, BigDecimal amount) {
        Account account = accountRepository.findByIBAN(IBAN)
                .orElseThrow(() -> new NotFoundException("Account with IBAN " + IBAN + " not found"));
        account.setBalance(account.getBalance().add(amount));
        return accountRepository.save(account);
    }
}
