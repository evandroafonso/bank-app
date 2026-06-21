package com.assignment.bank.account.service;

import com.assignment.bank.account.dto.AccountRequest;
import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.account.mapper.AccountMapper;
import com.assignment.bank.account.repository.AccountRepository;
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
}