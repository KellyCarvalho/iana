package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.out.ExpenseRepositoryPort;
import com.iana.domain.ports.out.StatementExtractorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseImportServiceTest {

    @Mock
    private ExpenseRepositoryPort expenseRepositoryPort;

    @Mock
    private StatementExtractorPort statementExtractorPort;

    @InjectMocks
    private ExpenseImportService expenseImportService;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        expenseImportService.setStatementExtractorPort(statementExtractorPort);
    }

    @Test
    @DisplayName("Deve importar despesas de fatura CSV estilo Nubank ignorando pagamentos recebidos")
    void shouldImportExpensesFromCsv() {
        String csv = """
                date,category,title,amount
                2026-11-01,restaurante,iFood *Restaurante,64.90
                2026-11-02,,Pagamento recebido,-1200.00
                2026-11-03,supermercado,Mercado Extra,342.15
                """;
        when(expenseRepositoryPort.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Expense> imported = expenseImportService.importFromCsv(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)),
                categoryId
        );

        assertThat(imported).hasSize(2);
        assertThat(imported.get(0).getName()).isEqualTo("iFood *Restaurante");
        assertThat(imported.get(0).getAmount()).isEqualByComparingTo("64.90");
        assertThat(imported.get(1).getName()).isEqualTo("Mercado Extra");
        assertThat(imported.get(1).getAmount()).isEqualByComparingTo("342.15");
    }

    @Test
    @DisplayName("Deve importar CSV mesmo com cabeçalhos desconhecidos ou sem template fixo")
    void shouldInferColumnsWithoutTemplate() {
        String unknownBankCsv = """
                col_x;col_y;col_z
                15/11/2026;Padaria Central;R$ 42,80
                16/11/2026;Farmacia Drogasil;R$ 115,00
                """;
        when(expenseRepositoryPort.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Expense> imported = expenseImportService.importFromCsv(
                new ByteArrayInputStream(unknownBankCsv.getBytes(StandardCharsets.UTF_8)),
                null
        );

        assertThat(imported).hasSize(2);
        assertThat(imported.get(0).getName()).isEqualTo("Padaria Central");
        assertThat(imported.get(0).getAmount()).isEqualByComparingTo("42.80");
        assertThat(imported.get(1).getName()).isEqualTo("Farmacia Drogasil");
        assertThat(imported.get(1).getAmount()).isEqualByComparingTo("115.00");
    }

    @Test
    @DisplayName("Deve importar despesas extraídas de PDF via StatementExtractorPort")
    void shouldImportExpensesFromPdf() {
        byte[] pdfBytes = "%PDF-1.4 sample".getBytes(StandardCharsets.UTF_8);
        Expense pdfExpense = new Expense(UUID.randomUUID(), "Boleto Condomínio", new BigDecimal("450.00"), "PDF", categoryId, LocalDateTime.now());
        when(statementExtractorPort.extractFromPdf(pdfBytes, categoryId)).thenReturn(List.of(pdfExpense));
        when(expenseRepositoryPort.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Expense> imported = expenseImportService.importFromPdf(pdfBytes, categoryId);

        assertThat(imported).hasSize(1);
        assertThat(imported.get(0).getName()).isEqualTo("Boleto Condomínio");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao importar CSV vazio")
    void shouldThrowWhenCsvIsEmpty() {
        assertThatThrownBy(() -> expenseImportService.importFromCsv(
                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
                null
        )).isInstanceOf(BusinessException.class);
    }
}
