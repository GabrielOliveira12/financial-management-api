package com.gabroliv.financemanagement.adapters.in.web.transaction;

import com.gabroliv.financemanagement.application.ports.in.TransactionUseCase;
import com.gabroliv.financemanagement.application.query.TransactionFilters;
import com.gabroliv.financemanagement.domain.model.Transaction;
import io.swagger.v3.oas.annotations.Parameter;
import org.apache.commons.csv.CSVFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/transactions")
public class TransactionReportController {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final TransactionUseCase transactionUseCase;

    public TransactionReportController(TransactionUseCase transactionUseCase) {
        this.transactionUseCase = transactionUseCase;
    }

    @GetMapping("/report")
    public ResponseEntity<byte[]> downloadReport(
            @AuthenticationPrincipal Long authenticatedUserId,
            @Parameter(description = "Start date (inclusive). Format: yyyy-MM-dd. Example: 2026-11-01")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End date (inclusive). Format: yyyy-MM-dd. Example: 2026-11-30")
            @RequestParam(required = false) LocalDate endDate) {

        TransactionFilters filters = new TransactionFilters(startDate, endDate, null);
        var transactions = transactionUseCase.findAllByUserId(authenticatedUserId, filters);

        byte[] csv = buildCsv(transactions);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("relatorio-transacoes.csv", StandardCharsets.UTF_8).build()
        );

        return ResponseEntity.ok().headers(headers).body(csv);
    }

    private byte[] buildCsv(Iterable<Transaction> transactions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(UTF8_BOM);

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(';')
                    .setHeader("Data", "Tipo", "Categoria", "Descrição", "Valor (R$)", "Status", "Motivo da Rejeição")
                    .build();

            try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                 var csvPrinter = format.print(writer)) {

                for (Transaction t : transactions) {
                    csvPrinter.printRecord(
                            t.getOccurredAt().format(DATE_FORMATTER),
                            t.getType(),
                            t.getCategory() != null ? t.getCategory() : "",
                            t.getDescription() != null ? t.getDescription() : "",
                            t.getAmount(),
                            t.getStatus(),
                            t.getRejectionReason() != null ? t.getRejectionReason() : ""
                    );
                }
            }

            return out.toByteArray();
        } catch (IOException exception) {
            throw new UncheckedIOException("Error generating CSV report.", exception);
        }
    }
}
