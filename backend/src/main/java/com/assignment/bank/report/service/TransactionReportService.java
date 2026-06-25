package com.assignment.bank.report.service;

import com.assignment.bank.exception.NotFoundException;
import com.assignment.bank.report.dto.TransactionReportRequest;
import com.assignment.bank.report.dto.TransactionReportResponse;
import com.assignment.bank.report.mapper.TransactionReportMapper;
import com.assignment.bank.transaction.entity.Transaction;
import com.assignment.bank.transaction.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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
        log.info("Initiating report generation for transaction UUID: {}", request.transactionUuid());
        Transaction transaction = repository.findByUuid(request.transactionUuid())
                .orElseThrow(() -> {
                    log.warn("Unable to generate report: Transaction not found with UUID: {}", request.transactionUuid());
                    return new NotFoundException("Transaction not found with uuid: " + request.transactionUuid());
                });

        log.debug("Transaction located. Proceeding with PDF compilation...");
        byte[] pdf = pdfService.generatePdf(reportMapper.mapToReportItem(transaction));

        log.info("Report successfully created for transaction UUID: {}", transaction.getUuid());
        return new TransactionReportResponse(
                pdf,
                "transaction-" + transaction.getUuid() + ".pdf"
        );
    }
}