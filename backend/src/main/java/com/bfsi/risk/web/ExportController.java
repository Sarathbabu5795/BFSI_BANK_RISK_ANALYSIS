package com.bfsi.risk.web;

import com.bfsi.risk.dto.CategoryCount;
import com.bfsi.risk.dto.DashboardSummary;
import com.bfsi.risk.dto.TransactionFlag;
import com.bfsi.risk.rules.DataQualityService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = "http://localhost:4200")
public class ExportController {

    private final DashboardController dashboardController;
    private final DataQualityService dataQualityService;

    public ExportController(DashboardController dashboardController, DataQualityService dataQualityService) {
        this.dashboardController = dashboardController;
        this.dataQualityService = dataQualityService;
    }

    @GetMapping("/report.pdf")
    public ResponseEntity<byte[]> report() throws Exception {
        DashboardSummary summary = dashboardController.summary();
        List<TransactionFlag> flags = dataQualityService.allFlags();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(out)); Document doc = new Document(pdf)) {
            doc.add(new Paragraph("BFSI Banking Risk Analysis Report")
                    .setFontSize(20).setBold());
            doc.add(new Paragraph("Generated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .setFontColor(ColorConstants.GRAY).setFontSize(10));

            doc.add(new Paragraph("Dataset Overview").setFontSize(14).setBold().setMarginTop(16));
            Table overview = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            overview.setWidth(UnitValue.createPercentValue(100));
            addRow(overview, "Customers", String.valueOf(summary.totalCustomers()));
            addRow(overview, "Accounts", String.valueOf(summary.totalAccounts()));
            addRow(overview, "Transactions", String.valueOf(summary.totalTransactions()));
            addRow(overview, "Incidents", String.valueOf(summary.totalIncidents()));
            addRow(overview, "API log entries", String.valueOf(summary.totalApiLogs()));
            doc.add(overview);

            doc.add(new Paragraph("Risk & Data Quality Findings").setFontSize(14).setBold().setMarginTop(16));
            Table findings = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
            findings.setWidth(UnitValue.createPercentValue(100));
            addRow(findings, "Total flagged transactions", String.valueOf(summary.flaggedTransactionCount()));
            addRow(findings, "High-risk transactions (risk_score >= 70)", String.valueOf(summary.highRiskTransactionCount()));
            addRow(findings, "Risk label mismatches", String.valueOf(summary.labelMismatchCount()));
            addRow(findings, "Average risk score", String.valueOf(summary.averageRiskScore()));
            addRow(findings, "SLA-breached incidents", String.valueOf(summary.slaBreachCount()));
            addRow(findings, "Incident status/date conflicts", String.valueOf(summary.statusDateConflictCount()));
            addRow(findings, "Slow API calls (>2000ms)", String.valueOf(summary.slowApiCount()));
            addRow(findings, "Server failures (5xx)", String.valueOf(summary.serverFailureCount()));
            doc.add(findings);

            doc.add(new Paragraph("Flags by Rule").setFontSize(14).setBold().setMarginTop(16));
            Table byRule = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
            byRule.setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(byRule, "Rule", "Count");
            for (CategoryCount c : summary.flagsByRule()) {
                addRow(byRule, c.category(), String.valueOf(c.count()));
            }
            doc.add(byRule);

            doc.add(new Paragraph("Sample Flagged Transactions (first 25)").setFontSize(14).setBold().setMarginTop(16));
            Table sample = new Table(UnitValue.createPercentArray(new float[]{1, 1, 2, 3}));
            sample.setWidth(UnitValue.createPercentValue(100));
            addHeaderRow(sample, "Rule", "Transaction ID", "Amount", "Reason");
            flags.stream().limit(25).forEach(f -> {
                sample.addCell(new Cell().add(new Paragraph(f.ruleCode())));
                sample.addCell(new Cell().add(new Paragraph(f.transaction().transactionId())));
                sample.addCell(new Cell().add(new Paragraph(f.transaction().transactionAmount().toString() + " " + f.transaction().currency())));
                sample.addCell(new Cell().add(new Paragraph(f.reason())));
            });
            doc.add(sample);

            doc.add(new Paragraph("This report is a rules-based, automated risk detection summary. "
                    + "It is an AI-assisted recommendation only - final banking decisions require human review.")
                    .setFontSize(9).setFontColor(ColorConstants.GRAY).setMarginTop(16));
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "bfsi-risk-analysis-report.pdf");
        return ResponseEntity.ok().headers(headers).body(out.toByteArray());
    }

    private static void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value)));
    }

    private static void addHeaderRow(Table table, String... headers) {
        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h).setBold()));
        }
    }
}
