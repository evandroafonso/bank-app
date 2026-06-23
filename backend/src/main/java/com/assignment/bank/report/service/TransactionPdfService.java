package com.assignment.bank.report.service;

import com.assignment.bank.report.dto.TransactionReportItemResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class TransactionPdfService {

    public byte[] generatePdf(TransactionReportItemResponse item) {

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
            addRow(table, "Source Amount", item.sourceAmount().toString());

            if (item.convertedAmount() != null) {
                addRow(table, "Converted Amount", item.convertedAmount().toString());
            }
            if (item.exchangeRate() != null) {
                addRow(table, "Exchange Rate", item.exchangeRate().toString());
            }

            addRow(table, "Currency", item.currency());

            if (item.targetCurrency() != null) {
                addRow(table, "Target Currency", item.targetCurrency());
            }

            addRow(table, "Balance", item.balance().toString());
            addRow(table, "Type", item.type());
            addRow(table, "Description", item.description());
            addRow(table, "Timestamp", item.timestamp().toString());

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private void addRow(PdfPTable table, String key, String value) {
        table.addCell(key);
        table.addCell(value != null ? value : "");
    }
}