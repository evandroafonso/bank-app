package com.assignment.bank.account.service;

import com.assignment.bank.account.dto.AccountRequest;
import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.account.mapper.AccountMapper;
import com.assignment.bank.account.repository.AccountRepository;
import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        AccountRequest request = new AccountRequest(Currency.EUR);
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        User user = mock(User.class);
        AccountResponse response = mock(AccountResponse.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("john@email.com");
        when(userService.findByEmail("john@email.com")).thenReturn(user);
        when(accountMapper.mapToResponse(any(Account.class))).thenReturn(response);

        AccountResponse result = accountService.save(request);

        assertNotNull(result);
        assertEquals(response, result);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> accountService.save(new AccountRequest(Currency.EUR)));

        verifyNoInteractions(accountRepository, userService, accountMapper);
    }

    @Test
    void shouldThrowExceptionWhenPrincipalIsNotUserDetails() {
        Authentication auth = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("not-a-user-details");

        assertThrows(IllegalStateException.class,
                () -> accountService.save(new AccountRequest(Currency.EUR)));
    }

    @Test
    void shouldThrowExceptionWhenRepositoryFails() {
        AccountRequest request = new AccountRequest(Currency.EUR);
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        User user = mock(User.class);

        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("john@email.com");
        when(userService.findByEmail("john@email.com")).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenThrow(new RuntimeException("DB error"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> accountService.save(request));

        assertEquals("DB error", ex.getMessage());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldFindAllAccountsSuccessfully() {
        Account account = mock(Account.class);
        AccountResponse response = mock(AccountResponse.class);

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(accountMapper.mapToResponse(account)).thenReturn(response);

        List<AccountResponse> result = accountService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
        verify(accountRepository).findAll();
        verify(accountMapper).mapToResponse(account);
    }

    @Test
    void shouldReturnEmptyListWhenNoAccountsExist() {
        when(accountRepository.findAll()).thenReturn(List.of());

        List<AccountResponse> result = accountService.findAll();

        assertTrue(result.isEmpty());
        verify(accountRepository).findAll();
        verifyNoInteractions(accountMapper);
    }

    @Test
    void shouldFindByIBANSuccessfully() {
        String iban = "EE12345678901234";
        Account account = mock(Account.class);
        AccountResponse response = mock(AccountResponse.class);

        when(accountRepository.findByIBAN(iban)).thenReturn(Optional.of(account));
        when(accountMapper.mapToResponse(account)).thenReturn(response);

        AccountResponse result = accountService.findByIBAN(iban);

        assertNotNull(result);
        assertEquals(response, result);
        verify(accountRepository).findByIBAN(iban);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenIBANDoesNotExist() {
        String iban = "INVALID-IBAN";

        when(accountRepository.findByIBAN(iban)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> accountService.findByIBAN(iban));

        verify(accountRepository).findByIBAN(iban);
        verifyNoInteractions(accountMapper);
    }

}