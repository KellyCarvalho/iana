package com.iana.infrastructure.adapters.out.ai;

import com.iana.domain.model.Expense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AiStatementExtractorAdapterTest {

    private final AiStatementExtractorAdapter adapter = new AiStatementExtractorAdapter("", "gemini-2.5-flash");

    @Test
    @DisplayName("Deve extrair despesas de PDF de fatura com datas numéricas e por extenso (Nubank/Itaú/Inter)")
    void shouldExtractTransactionsFromPdfBytes() {
        String fakePdfContent = """
                %PDF-1.4
                1 0 obj
                << /Length 120 >>
                stream
                (15/10 Supermercado Extra R$ 342,15) Tj
                (18 OUT Uber *Trip R$ 28,50) Tj
                (20/10 Pagamento recebido -1000,00) Tj
                endstream
                endobj
                %%EOF
                """;

        UUID categoryId = UUID.randomUUID();
        List<Expense> expenses = adapter.extractFromPdf(fakePdfContent.getBytes(StandardCharsets.UTF_8), categoryId);

        assertThat(expenses).hasSize(2);
        assertThat(expenses.get(0).getName()).isEqualTo("Supermercado Extra");
        assertThat(expenses.get(0).getAmount()).isEqualByComparingTo("342.15");
        assertThat(expenses.get(1).getName()).isEqualTo("Uber *Trip");
        assertThat(expenses.get(1).getAmount()).isEqualByComparingTo("28.50");
    }

    @Test
    @DisplayName("Deve extrair valor e banco automaticamente de PDF de Boleto Bancário (Linha Digitável FEBRABAN)")
    void shouldExtractBoletoFromPdfBytes() {
        // Linha digitável de 47 dígitos terminando em 0000018550 (R$ 185,50) do banco 341 (Itaú)
        String boletoPdf = """
                %PDF-1.4
                stream
                (Boleto de Condominio) Tj
                (34191.79001 01043.510047 91020.150008 8 98850000018550) Tj
                endstream
                """;

        List<Expense> expenses = adapter.extractFromPdf(boletoPdf.getBytes(StandardCharsets.UTF_8), null);

        assertThat(expenses).hasSize(1);
        assertThat(expenses.get(0).getName()).contains("Boleto Bancário (Banco 341)");
        assertThat(expenses.get(0).getAmount()).isEqualByComparingTo("185.50");
    }
}
