package com.assignment.bank.account.mapper;

import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.entity.Account;
import com.assignment.bank.user.dto.UserResponse;
import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountMapperTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AccountMapper accountMapper;

    @Test
    void shouldMapAccountEntityToResponseCorrectly() {
        UUID accountUuid = UUID.randomUUID();
        User owner = User.builder().username("john").build();
        UserResponse userResponse = UserResponse.builder().username("john").build();

        Account account = Account.builder()
                .uuid(accountUuid)
                .IBAN("EE12345678901234")
                .currency(com.assignment.bank.account.enums.Currency.EUR)
                .balance(new BigDecimal("100.00"))
                .owner(owner)
                .build();

        when(userMapper.mapToResponse(owner)).thenReturn(userResponse);

        AccountResponse response = accountMapper.mapToResponse(account);

        assertNotNull(response);
        assertEquals(accountUuid.toString(), response.uuid());
        assertEquals("EE12345678901234", response.IBAN());
        assertEquals("EUR", response.currency());
        assertEquals(new BigDecimal("100.00"), response.balance());
        assertEquals(userResponse, response.user());

        verify(userMapper).mapToResponse(owner);
    }

    @Test
    void shouldReturnNullWhenAccountIsNull() {
        AccountResponse response = accountMapper.mapToResponse(null);
        assertNull(response);
        verifyNoInteractions(userMapper);
    }
}