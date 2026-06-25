package com.assignment.bank.account.entity;

import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.common.entity.BaseEntity;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@SuperBuilder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    @Column(nullable = false, unique = true)
    @NotBlank
    @Size(max = 34)
    private String iban;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Builder.Default
    @Column(nullable = false, precision = 20, scale = 8)
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Builder.Default
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

}
