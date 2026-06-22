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
import com.assignment.bank.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private FraudDetectorService fraudDetectorService;

    @InjectMocks
    private TransactionService transactionService;

    private User mockUser() {
        User user = mock(User.class);
        when(authenticatedUserProvider.get()).thenReturn(user);
        return user;
    }

    @Test
    void shouldCreditAccountSuccessfully() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("100.00"), Currency.EUR, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(new BigDecimal("500.00")).build();
        Transaction savedTransaction = mock(Transaction.class);
        TransactionResponse response = mock(TransactionResponse.class);

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));
        when(transactionMapper.mapToEntity(eq(request), eq(account), eq(TransactionType.CREDIT), any(), any(), any())).thenReturn(mock(Transaction.class));
        when(transactionRepository.save(any())).thenReturn(savedTransaction);
        when(transactionMapper.mapToResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = transactionService.credit(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("600.00"), account.getBalance());
        verify(accountRepository).findByIbanAndOwner("EE382200221020145685", user);
        verify(transactionRepository).save(any());
        verify(transactionMapper).mapToResponse(savedTransaction);
    }

    @Test
    void shouldCreditAccountWithCurrencyConversion() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("100.00"), Currency.USD, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(new BigDecimal("500.00")).build();
        Transaction savedTransaction = mock(Transaction.class);
        TransactionResponse response = mock(TransactionResponse.class);

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));
        when(exchangeRateProvider.getRate(Currency.USD, Currency.EUR)).thenReturn(new BigDecimal("0.85"));
        when(transactionMapper.mapToEntity(eq(request), eq(account), eq(TransactionType.CREDIT), any(), any(), eq(new BigDecimal("0.85")))).thenReturn(mock(Transaction.class));
        when(transactionRepository.save(any())).thenReturn(savedTransaction);
        when(transactionMapper.mapToResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = transactionService.credit(request);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("585.00").compareTo(account.getBalance()));
        verify(exchangeRateProvider).getRate(Currency.USD, Currency.EUR);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExistOnCredit() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("INVALID", new BigDecimal("100.00"), Currency.EUR, "test");

        when(accountRepository.findByIbanAndOwner("INVALID", user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.credit(request));

        verifyNoInteractions(transactionMapper, transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenMapperToEntityFailsOnCredit() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("100.00"), Currency.EUR, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(new BigDecimal("500.00")).build();

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));
        when(transactionMapper.mapToEntity(any(), any(), any(), any(), any(), any())).thenThrow(new RuntimeException("Mapper error"));

        assertThrows(RuntimeException.class, () -> transactionService.credit(request));

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldDebitAccountSuccessfully() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("100.00"), Currency.EUR, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(new BigDecimal("500.00")).build();
        Transaction savedTransaction = mock(Transaction.class);
        TransactionResponse response = mock(TransactionResponse.class);

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));
        when(transactionMapper.mapToEntity(eq(request), eq(account), eq(TransactionType.DEBIT), any(), any(), any())).thenReturn(mock(Transaction.class));
        when(transactionRepository.save(any())).thenReturn(savedTransaction);
        when(transactionMapper.mapToResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = transactionService.debit(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("400.00"), account.getBalance());
        verify(accountRepository).findByIbanAndOwner("EE382200221020145685", user);
        verify(transactionRepository).save(any());
        verify(transactionMapper).mapToResponse(savedTransaction);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExistOnDebit() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("INVALID", new BigDecimal("100.00"), Currency.EUR, "test");

        when(accountRepository.findByIbanAndOwner("INVALID", user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.debit(request));

        verifyNoInteractions(transactionMapper, transactionRepository);
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionOnDebit() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("1000.00"), Currency.EUR, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(new BigDecimal("500.00")).build();

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.debit(request));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowInsufficientBalanceWhenBalanceEqualsZeroOnDebit() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("0.0001"), Currency.EUR, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(BigDecimal.ZERO).build();

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.debit(request));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowCurrencyMismatchExceptionOnDebit() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("100.00"), Currency.USD, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(new BigDecimal("500.00")).build();

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));
        when(exchangeRateProvider.getRate(Currency.USD, Currency.EUR)).thenReturn(new BigDecimal("0.87"));

        assertThrows(CurrencyMismatchException.class, () -> transactionService.debit(request));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowFraudDetectedExceptionOnDebit() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("100.00"), Currency.EUR, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(new BigDecimal("500.00")).build();

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));
        doThrow(new com.assignment.bank.exception.FraudDetectedException("fraud")).when(fraudDetectorService).check(any());

        assertThrows(com.assignment.bank.exception.FraudDetectedException.class, () -> transactionService.debit(request));

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNullOnCredit() {
        TransactionRequest request = new TransactionRequest("EE382200221020145685", null, Currency.EUR, "test");

        assertThrows(IllegalArgumentException.class, () -> transactionService.credit(request));

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenAmountIsZeroOnCredit() {
        TransactionRequest request = new TransactionRequest("EE382200221020145685", BigDecimal.ZERO, Currency.EUR, "test");

        assertThrows(IllegalArgumentException.class, () -> transactionService.credit(request));

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNegativeOnCredit() {
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("-50.00"), Currency.EUR, "test");

        assertThrows(IllegalArgumentException.class, () -> transactionService.credit(request));

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenAmountExceedsFourDecimalPlacesOnCredit() {
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("10.000001"), Currency.EUR, "test");

        assertThrows(IllegalArgumentException.class, () -> transactionService.credit(request));

        verifyNoInteractions(accountRepository, transactionRepository);
    }

    @Test
    void shouldAcceptAmountWithExactlyFourDecimalPlaces() {
        User user = mockUser();
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("10.0001"), Currency.EUR, "test");
        Account account = Account.builder().currency(Currency.EUR).balance(new BigDecimal("500.00")).build();
        Transaction savedTransaction = mock(Transaction.class);

        when(accountRepository.findByIbanAndOwner("EE382200221020145685", user)).thenReturn(Optional.of(account));
        when(transactionMapper.mapToEntity(any(), any(), any(), any(), any(), any())).thenReturn(mock(Transaction.class));
        when(transactionRepository.save(any())).thenReturn(savedTransaction);
        when(transactionMapper.mapToResponse(savedTransaction)).thenReturn(mock(TransactionResponse.class));

        assertDoesNotThrow(() -> transactionService.credit(request));
    }
}