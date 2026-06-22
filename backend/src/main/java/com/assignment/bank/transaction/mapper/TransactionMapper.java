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

    public Transaction mapToEntity(
            TransactionRequest request,
            Account account,
            TransactionType transactionType,
            BigDecimal balanceAfter,
            BigDecimal convertedAmount,
            BigDecimal exchangeRate
    ) {
        if (request == null || account == null) {
            return null;
        }

        return Transaction.builder()
                .account(account)
                .sourceCurrency(request.currency())
                .targetCurrency(account.getCurrency())
                .sourceAmount(request.amount())
                .convertedAmount(convertedAmount)
                .exchangeRate(exchangeRate)
                .balanceAfter(balanceAfter)
                .type(transactionType)
                .description(request.description())
                .build();
    }

    public TransactionResponse mapToResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .uuid(transaction.getUuid().toString())
                .sourceAmount(transaction.getSourceAmount())
                .convertedAmount(transaction.getConvertedAmount())
                .exchangeRate(transaction.getExchangeRate())
                .type(transaction.getType())
                .currency(transaction.getSourceCurrency())
                .targetCurrency(transaction.getTargetCurrency())
                .balance(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .timestamp(transaction.getCreatedAt())
                .build();
    }
}