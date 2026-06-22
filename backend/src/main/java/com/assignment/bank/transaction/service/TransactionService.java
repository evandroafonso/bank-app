package com.assignment.bank.transaction.service;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.account.repository.AccountRepository;
import com.assignment.bank.exception.CurrencyMismatchException;
import com.assignment.bank.exception.InsufficientBalanceException;
import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.security.AuthenticatedUserProvider;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.enums.TransactionType;
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
    private final ExchangeRateProvider exchangeRateProvider;
    private final AuthenticatedUserProvider authenticatedUserProvider;


    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              TransactionMapper transactionMapper,
                              ExchangeRateProvider exchangeRateProvider,
                              AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.exchangeRateProvider = exchangeRateProvider;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Transactional
    public TransactionResponse credit(TransactionRequest request) {
        return processTransaction(request, TransactionType.CREDIT);
    }

    @Transactional
    public TransactionResponse debit(TransactionRequest request) {
        return processTransaction(request, TransactionType.DEBIT);
    }

    private TransactionResponse processTransaction(TransactionRequest request, TransactionType type) {
        validateAmount(request.amount());

        Account account = getAccount(request.iban());

        BigDecimal exchangeRate = getExchangeRate(request.currency(), account.getCurrency());
        BigDecimal convertedAmount = convert(request.amount(), exchangeRate);

        validateTransaction(type, request.currency(), account, convertedAmount);

        BigDecimal newBalance = calculateNewBalance(account, convertedAmount, type);
        updateAccountBalance(account, newBalance);

        Transaction transaction = transactionMapper.mapToEntity(
                request, account, type, newBalance, convertedAmount, exchangeRate
        );

        return transactionMapper.mapToResponse(transactionRepository.save(transaction));
    }

    private BigDecimal getExchangeRate(Currency source, Currency target) {
        if (source == target) return BigDecimal.ONE;
        return exchangeRateProvider.getRate(source, target);
    }

    private BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate);
    }

    private void validateTransaction(TransactionType type, Currency requestCurrency, Account account, BigDecimal amount) {
        if (type == TransactionType.DEBIT) {
            validateCurrencyMatch(requestCurrency, account.getCurrency());
            validateSufficientBalance(account, amount);
        }
    }

    private void validateCurrencyMatch(Currency requestCurrency, Currency accountCurrency) {
        if (requestCurrency != accountCurrency) {
            throw new CurrencyMismatchException(
                    "Debit currency must match account currency. Expected: " + accountCurrency + ", got: " + requestCurrency
            );
        }
    }


    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
    }

    private BigDecimal calculateNewBalance(Account account,
                                           BigDecimal amount,
                                           TransactionType type) {
        return switch (type) {
            case CREDIT -> account.getBalance().add(amount);
            case DEBIT -> account.getBalance().subtract(amount);
        };
    }

    private void updateAccountBalance(Account account, BigDecimal newBalance) {
        account.setBalance(newBalance);
    }

    private @NonNull Account getAccount(String iban) {
        return accountRepository.findByIbanAndOwner(iban, authenticatedUserProvider.get())
                .orElseThrow(() -> new NotFoundException("Account with iban " + iban + " not found"));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (amount.scale() > 4) {
            throw new IllegalArgumentException("Amount cannot have more than 4 decimal places");
        }
    }
}