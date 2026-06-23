package com.assignment.bank.report.mapper;

import com.assignment.bank.report.dto.TransactionReportItemResponse;
import com.assignment.bank.transaction.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionReportMapper {

    public TransactionReportItemResponse mapToReportItem(Transaction t) {
        boolean hasCurrencyConversion = t.getSourceCurrency() != t.getTargetCurrency();

        return new TransactionReportItemResponse(
                t.getUuid(),
                t.getSourceAmount(),
                hasCurrencyConversion ? t.getConvertedAmount() : null,
                hasCurrencyConversion ? t.getExchangeRate() : null,
                t.getSourceCurrency().name(),
                hasCurrencyConversion ? t.getTargetCurrency().name() : null,
                t.getBalanceAfter(),
                t.getType().name(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}