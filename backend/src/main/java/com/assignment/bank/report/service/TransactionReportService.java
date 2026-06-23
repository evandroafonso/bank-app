package com.assignment.bank.report.service;

import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.report.dto.TransactionReportRequest;
import com.assignment.bank.report.dto.TransactionReportResponse;
import com.assignment.bank.report.mapper.TransactionReportMapper;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

@Service
public class TransactionReportService {

    private final TransactionRepository repository;
    private final TransactionPdfService pdfService;
    private final TransactionReportMapper reportMapper;


    public TransactionReportService(TransactionRepository repository,
                                    TransactionPdfService pdfService,
                                    TransactionReportMapper reportMapper) {
        this.repository = repository;
        this.pdfService = pdfService;
        this.reportMapper = reportMapper;
    }

    public TransactionReportResponse generate(TransactionReportRequest request) {

        Transaction transaction = repository.findByUuid(request.transactionUuid())
                .orElseThrow(() -> new NotFoundException("Transaction not found with uuid: " + request.transactionUuid()));

        byte[] pdf = pdfService.generatePdf(reportMapper.mapToReportItem(transaction));

        return new TransactionReportResponse(
                pdf,
                "transaction-" + transaction.getUuid() + ".pdf"
        );
    }
}