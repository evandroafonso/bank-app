package com.assignment.bank.transaction.repository;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccountOrderByCreatedAtDesc(Account account, Pageable pageable);
}
