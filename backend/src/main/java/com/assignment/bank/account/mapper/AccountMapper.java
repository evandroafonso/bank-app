package com.assignment.bank.account.mapper;

import com.assignment.bank.account.dto.AccountBalanceResponse;
import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.entity.Account;
import com.assignment.bank.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    private final UserMapper userMapper;

    public AccountMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public AccountResponse mapToResponse(Account account) {
        if (account == null) {
            return null;
        }

        return AccountResponse.builder()
                .uuid(account.getUuid().toString())
                .iban(account.getIban())
                .currency(account.getCurrency().toString())
                .balance(account.getBalance())
                .user(userMapper.mapToResponse(account.getOwner()))
                .build();
    }

    public AccountBalanceResponse mapToBalanceResponse(Account account) {
        if (account == null) {
            return null;
        }

        return new AccountBalanceResponse(
                account.getIban(),
                account.getCurrency().toString(),
                account.getBalance()
        );
    }
}