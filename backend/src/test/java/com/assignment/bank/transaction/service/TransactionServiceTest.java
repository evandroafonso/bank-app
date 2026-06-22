package com.assignment.bank.transaction.service;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.repository.AccountRepository;
import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.mapper.TransactionMapper;
import com.assignment.bank.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldCreditAccountSuccessfully() {
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("100.00"), "test");
        Account account = Account.builder().balance(new BigDecimal("500.00")).build();
        Transaction transaction = mock(Transaction.class);
        Transaction savedTransaction = mock(Transaction.class);
        TransactionResponse response = mock(TransactionResponse.class);

        when(accountRepository.findByIban("EE382200221020145685")).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(transactionMapper.mapToEntity(request, account)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(savedTransaction);
        when(transactionMapper.mapToResponse(savedTransaction)).thenReturn(response);

        TransactionResponse result = transactionService.credit(request);

        assertNotNull(result);
        assertEquals(new BigDecimal("600.00"), account.getBalance());

        verify(accountRepository).findByIban("EE382200221020145685");
        verify(accountRepository).save(account);
        verify(transactionRepository).save(transaction);
        verify(transactionMapper).mapToResponse(savedTransaction);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExist() {
        TransactionRequest request = new TransactionRequest("INVALID", new BigDecimal("100.00"), "test");

        when(accountRepository.findByIban("INVALID")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> transactionService.credit(request));

        verify(accountRepository).findByIban("INVALID");
        verifyNoInteractions(transactionMapper);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldThrowExceptionWhenMapperToEntityFails() {
        TransactionRequest request = new TransactionRequest("EE382200221020145685", new BigDecimal("100.00"), "test");
        Account account = mock(Account.class);

        when(accountRepository.findByIban("EE123")).thenReturn(Optional.of(account));
        when(accountRepository.save(any())).thenReturn(account);
        when(transactionMapper.mapToEntity(any(), any())).thenThrow(new RuntimeException("Mapper error"));

        assertThrows(RuntimeException.class, () -> transactionService.credit(request));

        verify(transactionRepository, never()).save(any());
    }
}