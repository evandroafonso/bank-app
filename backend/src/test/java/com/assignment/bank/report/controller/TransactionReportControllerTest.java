package com.assignment.bank.report.controller;

import com.assignment.bank.report.dto.TransactionReportRequest;
import com.assignment.bank.report.dto.TransactionReportResponse;
import com.assignment.bank.report.service.TransactionReportService;
import com.assignment.bank.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionReportControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionReportService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGeneratePdfSuccessfully() throws Exception {

        byte[] fakePdf = "fake-pdf-content".getBytes();

        TransactionReportResponse response =
                new TransactionReportResponse(fakePdf, "transaction-report.pdf");

        when(service.generate(any(TransactionReportRequest.class)))
                .thenReturn(response);

        TransactionReportRequest request =
                new TransactionReportRequest(java.util.UUID.randomUUID());

        mockMvc.perform(post("/api/reports/transaction/pdf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=transaction-report.pdf"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(fakePdf));
    }
}