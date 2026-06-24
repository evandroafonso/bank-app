package com.assignment.bank.account.service;

import com.assignment.bank.account.dto.AccountBalanceResponse;
import com.assignment.bank.account.dto.AccountRequest;
import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.account.mapper.AccountMapper;
import com.assignment.bank.account.repository.AccountRepository;
import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.security.AuthenticatedUserProvider;
import com.assignment.bank.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private AccountService accountService;

    private User mockUser() {
        User user = mock(User.class);
        when(authenticatedUserProvider.get()).thenReturn(user);
        return user;
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        User user = mockUser();
        AccountRequest request = new AccountRequest(Currency.EUR);
        AccountResponse response = mock(AccountResponse.class);

        when(accountMapper.mapToResponse(any(Account.class))).thenReturn(response);

        AccountResponse result = accountService.save(request);

        assertNotNull(result);
        assertEquals(response, result);
        verify(accountRepository).save(any(Account.class));
        verify(authenticatedUserProvider).get();
    }

    @Test
    void shouldThrowExceptionWhenUserNotAuthenticated() {
        when(authenticatedUserProvider.get())
                .thenThrow(new AuthenticationCredentialsNotFoundException("User not authenticated"));

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> accountService.save(new AccountRequest(Currency.EUR)));

        verifyNoInteractions(accountRepository, accountMapper);
    }

    @Test
    void shouldThrowExceptionWhenRepositoryFails() {
        mockUser();
        AccountRequest request = new AccountRequest(Currency.EUR);

        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> accountService.save(request));

        assertEquals("DB error", ex.getMessage());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldFindAllAccountsForLoggedUser() {
        User user = mockUser();
        Account account = mock(Account.class);
        AccountResponse response = mock(AccountResponse.class);

        when(accountRepository.findByOwner(user)).thenReturn(List.of(account));
        when(accountMapper.mapToResponse(account)).thenReturn(response);

        List<AccountResponse> result = accountService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
        verify(accountRepository).findByOwner(user);
        verify(accountMapper).mapToResponse(account);
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExistForLoggedUser() {
        User user = mockUser();

        when(accountRepository.findByOwner(user)).thenReturn(List.of());

        List<AccountResponse> result = accountService.findAll();

        assertTrue(result.isEmpty());
        verify(accountRepository).findByOwner(user);
        verifyNoInteractions(accountMapper);
    }

    @Test
    void shouldFindByIbanAndOwnerSuccessfully() {
        User user = mockUser();
        String iban = "EE12345678901234";
        Account account = mock(Account.class);
        AccountResponse response = mock(AccountResponse.class);

        when(accountRepository.findByIbanAndOwner(iban, user)).thenReturn(Optional.of(account));
        when(accountMapper.mapToResponse(account)).thenReturn(response);

        AccountResponse result = accountService.findByIban(iban);

        assertNotNull(result);
        assertEquals(response, result);
        verify(accountRepository).findByIbanAndOwner(iban, user);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenIbanDoesNotBelongToLoggedUser() {
        User user = mockUser();
        String iban = "INVALID-iban";

        when(accountRepository.findByIbanAndOwner(iban, user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.findByIban(iban));

        verify(accountRepository).findByIbanAndOwner(iban, user);
        verifyNoInteractions(accountMapper);
    }

    @Test
    void shouldGetBalanceSuccessfully() {
        User user = mockUser();
        String iban = "EE12345678901234";
        Account account = mock(Account.class);
        AccountBalanceResponse response = mock(AccountBalanceResponse.class);

        when(accountRepository.findByIbanAndOwner(iban, user)).thenReturn(Optional.of(account));
        when(accountMapper.mapToBalanceResponse(account)).thenReturn(response);

        AccountBalanceResponse result = accountService.getBalance(iban);

        assertNotNull(result);
        assertEquals(response, result);
        verify(accountRepository).findByIbanAndOwner(iban, user);
        verify(accountMapper).mapToBalanceResponse(account);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGetBalanceForNonExistentAccount() {
        User user = mockUser();
        String iban = "INVALID-IBAN";

        when(accountRepository.findByIbanAndOwner(iban, user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.getBalance(iban));

        verify(accountRepository).findByIbanAndOwner(iban, user);
        verifyNoInteractions(accountMapper);
    }
}