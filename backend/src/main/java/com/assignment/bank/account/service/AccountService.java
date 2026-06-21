package com.assignment.bank.account.service;

import com.assignment.bank.account.dto.AccountRequest;
import com.assignment.bank.account.dto.AccountResponse;
import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.mapper.AccountMapper;
import com.assignment.bank.account.repository.AccountRepository;
import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.user.entity.User;
import com.assignment.bank.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserService userService;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, UserService userService, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.userService = userService;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public AccountResponse save(AccountRequest accountRequest) {
        User loggedUser = getAuthenticatedUser();

        Account account = new Account();
        account.setIBAN(generateIban());
        account.setOwner(loggedUser);
        account.setCurrency(accountRequest.currency());
        accountRepository.save(account);
        return accountMapper.mapToResponse(account);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated or security context missing.");
        }

        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userService.findByEmail(userDetails.getUsername());
        }

        throw new IllegalStateException("The principal is not an instance of UserDetails.");
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
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(accountMapper::mapToResponse)
                .toList();
    }

    public AccountResponse findByIBAN(String IBAN) {
        Account account = accountRepository.findByIBAN(IBAN)
                .orElseThrow(() -> new NotFoundException("Account not found with IBAN: " + IBAN));
        return accountMapper.mapToResponse(account);
    }

}