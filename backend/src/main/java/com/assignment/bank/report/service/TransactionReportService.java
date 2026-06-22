package com.assignment.bank.report.service;

import com.assignment.bank.report.dto.TransactionReportItem;
import com.assignment.bank.report.dto.TransactionReportRequest;
import com.assignment.bank.report.dto.TransactionReportResponse;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionReportService {

    private final TransactionRepository repository;
    private final TransactionPdfService pdfService;

    public TransactionReportService(TransactionRepository repository,
                                    TransactionPdfService pdfService) {
        this.repository = repository;
        this.pdfService = pdfService;
    }

    public TransactionReportResponse generate(TransactionReportRequest request) {

        Transaction transaction = repository.findByUuid(request.transactionUuid())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        TransactionReportItem item = map(transaction);

        byte[] pdf = pdfService.generatePdf(item);

        return new TransactionReportResponse(
                pdf,
                "transaction-" + transaction.getUuid() + ".pdf"
        );
    }

    private TransactionReportItem map(Transaction t) {
        return new TransactionReportItem(
                t.getUuid(),
                t.getSourceAmount(),
                t.getConvertedAmount(),
                t.getExchangeRate(),
                t.getSourceCurrency().name(),
                t.getTargetCurrency().name(),
                t.getBalanceAfter(),
                t.getType().name(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}