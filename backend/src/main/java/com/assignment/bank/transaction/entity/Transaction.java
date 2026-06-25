package com.assignment.bank.transaction.entity;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.common.entity.BaseEntity;
import com.assignment.bank.transaction.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "source_currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency sourceCurrency;

    @Column(name = "target_currency", nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency targetCurrency;

    @Column(name = "source_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal sourceAmount;

    @Column(name = "converted_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal convertedAmount;

    @Column(name = "exchange_rate", nullable = false, precision = 20, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "balance_after", nullable = false, precision = 20, scale = 8)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(length = 255)
    private String description;
}