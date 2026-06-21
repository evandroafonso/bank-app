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

        TransactionRequest request = new TransactionRequest("EE1234567890", new BigDecimal("100.00"), "Payment");

        Transaction transaction = transactionMapper.mapToEntity(request, account);

        assertNotNull(transaction);
        assertEquals(account, transaction.getAccount());
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals(TransactionType.CREDIT, transaction.getType());
        assertEquals(Currency.EUR, transaction.getCurrency());
        assertEquals("Payment", transaction.getDescription());
        assertEquals(BigDecimal.ONE, transaction.getExchangeRate());
    }

    @Test
    void shouldMapTransactionEntityToResponseCorrectly() {
        UUID txUuid = UUID.randomUUID();
        Account account = Account.builder().balance(new BigDecimal("1100.00")).build();
        LocalDateTime now = LocalDateTime.now();

        Transaction transaction = Transaction.builder()
                .uuid(txUuid)
                .amount(new BigDecimal("100.00"))
                .account(account)
                .currency(Currency.EUR)
                .description("Payment")
                .createdAt(now)
                .build();

        TransactionResponse response = transactionMapper.mapToResponse(transaction);

        assertNotNull(response);
        assertEquals(txUuid.toString(), response.transactionUuid());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals(new BigDecimal("1100.00"), response.balance());
        assertEquals(Currency.EUR, response.currency());
        assertEquals("Payment", response.description());
        assertEquals(now, response.timestamp());
    }

    @Test
    void shouldReturnNullWhenMappingToEntityWithNullInputs() {
        assertNull(transactionMapper.mapToEntity(null, null));
    }

    @Test
    void shouldReturnNullWhenMappingToResponseWithNullInput() {
        assertNull(transactionMapper.mapToResponse(null));
    }
}