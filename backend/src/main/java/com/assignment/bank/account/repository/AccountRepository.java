package com.assignment.bank.account.repository;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIbanAndOwner(String iban, User owner);

    List<Account> findByOwner(User owner);
}
