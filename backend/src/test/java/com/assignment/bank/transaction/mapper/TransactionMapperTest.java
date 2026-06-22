package com.assignment.bank.transaction.mapper;

import com.assignment.bank.account.entity.Account;
import com.assignment.bank.account.enums.Currency;
import com.assignment.bank.transaction.dto.TransactionRequest;
import com.assignment.bank.transaction.dto.TransactionResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionMapperTest {

    @InjectMocks
    private TransactionMapper transactionMapper;

    @Test
    void shouldMapRequestAndAccountToTransactionEntityCorrectly() {
        Account account = Account.builder()
                .currency(Currency.EUR)
                .balance(new BigDecimal("1000.00"))
                .build();

        TransactionRequest request = new TransactionRequest(
                "EE1234567890",
                new BigDecimal("100.00"),
                Currency.EUR,
                "Payment"
        );

        BigDecimal convertedAmount = new BigDecimal("100.00");
        BigDecimal balanceAfter = new BigDecimal("1100.00");
        BigDecimal exchangeRate = BigDecimal.ONE;

        Transaction transaction = transactionMapper.mapToEntity(
                request,
                account,
                TransactionType.CREDIT,
                balanceAfter,
                convertedAmount,
                exchangeRate
        );

        assertNotNull(transaction);
        assertEquals(account, transaction.getAccount());
        assertEquals(new BigDecimal("100.00"), transaction.getSourceAmount());
        assertEquals(convertedAmount, transaction.getConvertedAmount());
        assertEquals(exchangeRate, transaction.getExchangeRate());
        assertEquals(balanceAfter, transaction.getBalanceAfter());
        assertEquals(TransactionType.CREDIT, transaction.getType());
        assertEquals(Currency.EUR, transaction.getSourceCurrency());
        assertEquals(Currency.EUR, transaction.getTargetCurrency());
        assertEquals("Payment", transaction.getDescription());
    }

    @Test
    void shouldMapTransactionEntityToResponseCorrectly() {
        UUID txUuid = UUID.randomUUID();

        Account account = Account.builder()
                .currency(Currency.USD)
                .balance(new BigDecimal("1100.00"))
                .build();

        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = Transaction.builder()
                .uuid(txUuid)
                .sourceAmount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("117.00"))
                .exchangeRate(new BigDecimal("1.17"))
                .account(account)
                .sourceCurrency(Currency.EUR)
                .targetCurrency(Currency.USD)
                .balanceAfter(new BigDecimal("1100.00"))
                .type(TransactionType.CREDIT)
                .description("Payment")
                .createdAt(now)
                .build();

        TransactionResponse response = transactionMapper.mapToResponse(transaction);

        assertNotNull(response);
        assertEquals(txUuid.toString(), response.uuid());
        assertEquals(new BigDecimal("100.00"), response.sourceAmount());
        assertEquals(new BigDecimal("117.00"), response.convertedAmount());
        assertEquals(new BigDecimal("1.17"), response.exchangeRate());
        assertEquals(new BigDecimal("1100.00"), response.balance());
        assertEquals(Currency.EUR, response.currency());
        assertEquals(Currency.USD, response.targetCurrency());
        assertEquals(TransactionType.CREDIT, response.type());
        assertEquals("Payment", response.description());
        assertEquals(now, response.timestamp());
    }

    @Test
    void shouldReturnNullWhenMappingToEntityWithNullInputs() {
        assertNull(transactionMapper.mapToEntity(null, null, null, null, null, null));
    }

    @Test
    void shouldReturnNullWhenMappingToResponseWithNullInput() {
        assertNull(transactionMapper.mapToResponse(null));
    }
}