package com.assignment.bank.transaction.repository;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountOrderByCreatedAtDesc(Account account, Pageable pageable);

    Optional<Transaction> findByUuid(UUID uuid);

    List<Transaction> findByAccount_IbanAndCreatedAtBetweenOrderByCreatedAtAsc(
            String iban,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
