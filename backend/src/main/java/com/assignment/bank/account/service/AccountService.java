package com.assignment.bank.account.service;

import com.assignment.bank.account.dto.AccountBalanceResponse;
import com.assignment.bank.account.dto.AccountRequest;
import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.mapper.AccountMapper;
import com.assignment.bank.account.repository.AccountRepository;
import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.security.AuthenticatedUserProvider;
import com.assignment.bank.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository,
                          AuthenticatedUserProvider authenticatedUserProvider,
                          AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public AccountResponse save(AccountRequest accountRequest) {
        User loggedUser = authenticatedUserProvider.get();

        Account account = new Account();
        account.setIban(generateIban());
        account.setOwner(loggedUser);
        account.setCurrency(accountRequest.currency());
        accountRepository.save(account);
        return accountMapper.mapToResponse(account);
    }

    private String generateIban() {
        String countryCode = "EE";
        String bankCode = "22";

        SecureRandom random = new SecureRandom();
        StringBuilder accountNumber = new StringBuilder(14);

        for (int i = 0; i < 14; i++) {
            accountNumber.append(random.nextInt(10));
        }

        String bban = bankCode + accountNumber;
        String numericIbanString = bban + "141400";
        BigInteger numericIban = new BigInteger(numericIbanString);
        int mod97 = numericIban.remainder(new BigInteger("97")).intValue();
        int checkDigit = 98 - mod97;
        String checkDigitsStr = String.format("%02d", checkDigit);

        return countryCode + checkDigitsStr + bban;
    }

    public List<AccountResponse> findAll() {
        User loggedUser = authenticatedUserProvider.get();

        List<Account> accounts = accountRepository.findByOwner(loggedUser);
        return accounts.stream()
                .map(accountMapper::mapToResponse)
                .toList();
    }

    public AccountResponse findByIban(String iban) {
        User loggedUser = authenticatedUserProvider.get();
        Account account = accountRepository.findByIbanAndOwner(iban, loggedUser)
                .orElseThrow(() -> new NotFoundException("Account not found with iban: " + iban));
        return accountMapper.mapToResponse(account);
    }

    public AccountBalanceResponse getBalance(String iban) {
        User loggedUser = authenticatedUserProvider.get();
        Account account = accountRepository.findByIbanAndOwner(iban, loggedUser)
                .orElseThrow(() -> new NotFoundException("Account not found with iban: " + iban));

        return accountMapper.mapToBalanceResponse(account);
    }

}