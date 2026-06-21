package com.assignment.bank.transaction.mapper;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.enums.TransactionType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransactionMapper {

    public Transaction mapToEntity(TransactionRequest request, Account account) {
        if (request == null || account == null) {
            return null;
        }

        return Transaction.builder()
                .account(account)
                .amount(request.amount())
                .type(TransactionType.CREDIT)
                .currency(account.getCurrency())
                .exchangeRate(BigDecimal.ONE)
                .description(request.description())
                .build();
    }

    public TransactionResponse mapToResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .transactionUuid(transaction.getUuid().toString())
                .amount(transaction.getAmount())
                .balance(transaction.getAccount().getBalance())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .timestamp(transaction.getCreatedAt())
                .build();
    }
}
