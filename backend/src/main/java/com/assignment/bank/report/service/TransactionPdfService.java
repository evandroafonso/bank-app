package com.assignment.bank.report.service;

import com.assignment.bank.report.dto.TransactionReportItemResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
public class TransactionPdfService {

    public byte[] generatePdf(TransactionReportItemResponse item) {
        log.info("Starting PDF generation for transaction UUID: {}", item.uuid());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);

            Paragraph title = new Paragraph("Transaction Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addRow(table, "UUID", item.uuid().toString());

            addRow(table, "Source Amount", formatMonetaryAmount(item.sourceAmount()));

            if (item.convertedAmount() != null) {
                addRow(table, "Converted Amount", formatMonetaryAmount(item.convertedAmount()));
            }
            if (item.exchangeRate() != null) {
                addRow(table, "Exchange Rate", item.exchangeRate().setScale(8, RoundingMode.HALF_UP).toPlainString());
            }

            addRow(table, "Currency", item.currency());

            if (item.targetCurrency() != null) {
                addRow(table, "Target Currency", item.targetCurrency());
            }

            addRow(table, "Balance", formatMonetaryAmount(item.balance()));

            addRow(table, "Type", item.type());
            addRow(table, "Description", item.description());
            addRow(table, "Timestamp", item.timestamp().toString());

            document.add(table);
            document.close();

            log.info("PDF report successfully generated for transaction UUID: {}", item.uuid());
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate PDF report for transaction UUID: {}", item.uuid(), e);
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private void addRow(PdfPTable table, String key, String value) {
        table.addCell(key);
        table.addCell(value != null ? value : "");
    }

    private String formatMonetaryAmount(BigDecimal amount) {
        if (amount == null) return "";

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return "0.00";
        }

        int realScale = amount.stripTrailingZeros().scale();

        int finalScale = Math.clamp(realScale, 2, 8);

        return amount.setScale(finalScale, RoundingMode.HALF_UP).toPlainString();
    }
}