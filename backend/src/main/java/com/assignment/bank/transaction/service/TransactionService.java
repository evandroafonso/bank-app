package com.assignment.bank.transaction.service;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.account.repository.AccountRepository;
import com.assignment.bank.exception.CurrencyMismatchException;
import com.assignment.bank.exception.InsufficientBalanceException;
import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.security.AuthenticatedUserProvider;
import com.assignment.bank.transaction.dto.BalanceChartPointResponse;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.enums.TransactionType;
import com.assignment.bank.transaction.mapper.TransactionMapper;
import com.assignment.bank.transaction.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ExchangeRateProvider exchangeRateProvider;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final FraudDetectorService fraudDetectorService;

    public TransactionService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              TransactionMapper transactionMapper,
                              ExchangeRateProvider exchangeRateProvider,
                              AuthenticatedUserProvider authenticatedUserProvider,
                              FraudDetectorService fraudDetectorService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.transactionMapper = transactionMapper;
        this.exchangeRateProvider = exchangeRateProvider;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.fraudDetectorService = fraudDetectorService;
    }

    @Transactional
    public TransactionResponse credit(TransactionRequest request) {
        log.info("Processing credit transaction for IBAN: {}", request.iban());
        return processTransaction(request, TransactionType.CREDIT);
    }

    @Transactional
    public TransactionResponse debit(TransactionRequest request) {
        log.info("Processing debit transaction for IBAN: {}", request.iban());
        return processTransaction(request, TransactionType.DEBIT);
    }

    private TransactionResponse processTransaction(TransactionRequest request, TransactionType type) {
        validateAmount(request.amount());

        Account account = getAccount(request.iban());

        BigDecimal exchangeRate = getExchangeRate(request.currency(), account.getCurrency());
        BigDecimal convertedAmount = convert(request.amount(), exchangeRate);

        validateTransaction(type, request.currency(), account, convertedAmount, request.amount());

        BigDecimal newBalance = calculateNewBalance(account, convertedAmount, type);
        updateAccountBalance(account, newBalance);

        Transaction transaction = transactionMapper.mapToEntity(
                request, account, type, newBalance, convertedAmount, exchangeRate
        );

        log.info("Saving transaction of type {} for IBAN: {}", type, request.iban());
        return transactionMapper.mapToResponse(transactionRepository.save(transaction));
    }

    private BigDecimal getExchangeRate(Currency source, Currency target) {
        if (source == target) return BigDecimal.ONE;
        return exchangeRateProvider.getRate(source, target);
    }

    private BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate);
    }

    private void validateTransaction(TransactionType type, Currency requestCurrency, Account account, BigDecimal amount, BigDecimal requestAmount) {
        fraudDetectorService.check(requestAmount, account.getIban());
        if (type == TransactionType.DEBIT) {
            validateCurrencyMatch(requestCurrency, account.getCurrency());
            validateSufficientBalance(account, amount);
        }
    }

    private void validateCurrencyMatch(Currency requestCurrency, Currency accountCurrency) {
        if (requestCurrency != accountCurrency) {
            log.warn("Currency mismatch. Expected: {}, Got: {}", accountCurrency, requestCurrency);
            throw new CurrencyMismatchException(
                    "Debit currency must match account currency. Expected: " + accountCurrency + ", got: " + requestCurrency
            );
        }
    }


    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance for IBAN: {}. Available: {}, Required: {}", account.getIban(), account.getBalance(), amount);
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
                .orElseThrow(() -> {
                    log.warn("Account with iban {} not found for the authenticated user", iban);
                    return new NotFoundException("Account with iban " + iban + " not found");
                });
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            log.warn("Amount validation failed: value is null");
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Amount validation failed: value {} must be greater than zero", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (amount.scale() > 4) {
            log.warn("Amount validation failed: value {} cannot have more than 4 decimal places", amount);
            throw new IllegalArgumentException("Amount cannot have more than 4 decimal places");
        }
    }

    public Page<TransactionResponse> getHistory(String iban, Pageable pageable) {
        log.info("Fetching transaction history for IBAN: {}", iban);
        Account account = getAccount(iban);
        return transactionRepository.findByAccountOrderByCreatedAtDesc(account, pageable)
                .map(transactionMapper::mapToResponse);
    }

    public TransactionResponse getTransaction(UUID uuid) {
        log.info("Fetching transaction with UUID: {}", uuid);
        Transaction transaction = transactionRepository.findByUuid(uuid)
                .orElseThrow(() -> {
                    log.warn("Transaction with uuid {} not found", uuid);
                    return new NotFoundException("Transaction with uuid " + uuid + " not found");
                });
        return transactionMapper.mapToResponse(transaction);
    }

    public List<BalanceChartPointResponse> getBalanceChartData(String iban, LocalDateTime start, LocalDateTime end) {
        log.info("Fetching balance chart data for IBAN: {} from {} to {}", iban, start, end);
        List<Transaction> transactions = transactionRepository.findByAccount_IbanAndCreatedAtBetweenOrderByCreatedAtAsc(iban, start, end);

        return transactions.stream()
                .map(t -> new BalanceChartPointResponse(t.getCreatedAt(), t.getBalanceAfter()))
                .toList();
    }
}