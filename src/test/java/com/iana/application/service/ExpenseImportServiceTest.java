package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.model.Balance;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.in.ImportExpensesCsvUseCase.ImportResult;
import com.iana.domain.ports.out.BalanceRepositoryPort;
import com.iana.domain.ports.out.ExpenseRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseImportServiceTest {

    @Mock
    private ExpenseRepositoryPort expenseRepositoryPort;

    @Mock
    private BalanceRepositoryPort balanceRepositoryPort;

    @InjectMocks
    private ExpenseImportService expenseImportService;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        expenseImportService.setBalanceRepositoryPort(balanceRepositoryPort);
        lenient().when(expenseRepositoryPort.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(balanceRepositoryPort.save(any(Balance.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("Deve importar despesas e salário no modelo único Despesa | Meses | Salário")
    void shouldImportSingleModelWithExpensesAndSalary() {
        String csv = """
                Despesa;Janeiro;Fevereiro;Março
                Aluguel;1500,00;1500,00;1500,00
                Energia;180,50;-;210,00
                Total;1680,50;1500,00;1710,00
                Salário;4500,00;4500,00;4800,00
                """;

        ImportResult result = expenseImportService.importPlanilhaCsv(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)),
                categoryId
        );

        assertThat(result.expenses()).hasSize(5);
        assertThat(result.expenses().get(0).getName()).isEqualTo("Aluguel");
        assertThat(result.expenses().get(0).getAmount()).isEqualByComparingTo("1500.00");
        assertThat(result.expenses().get(0).getDescription()).isEqualTo("Mês: Janeiro");
        assertThat(result.expenses().get(0).getCategoryId()).isEqualTo(categoryId);

        assertThat(result.expenses().get(4).getName()).isEqualTo("Energia");
        assertThat(result.expenses().get(4).getAmount()).isEqualByComparingTo("210.00");
        assertThat(result.expenses().get(4).getDescription()).isEqualTo("Mês: Março");

        assertThat(result.balances()).hasSize(3);
        assertThat(result.balances().get(0).getSource()).isEqualTo("Salário");
        assertThat(result.balances().get(0).getAmount()).isEqualByComparingTo("4500.00");
        assertThat(result.balances().get(0).getDescription()).isEqualTo("Mês: Janeiro");
        assertThat(result.balances().get(2).getAmount()).isEqualByComparingTo("4800.00");
        assertThat(result.balances().get(2).getDescription()).isEqualTo("Mês: Março");
    }

    @Test
    @DisplayName("Deve importar CSV separado por vírgula e valores formatados com R$ e Windows-1252")
    void shouldImportCommaDelimitedAndWindows1252Csv() {
        String csv = "Despesa,Janeiro,Fevereiro\n"
                + "Internet,\"R$ 120,00\",\"R$ 135,50\"\n"
                + "Salário,\"R$ 5.000,00\",\"R$ 5.000,00\"\n";

        byte[] windows1252Bytes = csv.getBytes(Charset.forName("windows-1252"));
        ImportResult result = expenseImportService.importPlanilhaCsv(
                new ByteArrayInputStream(windows1252Bytes),
                null
        );

        assertThat(result.expenses()).hasSize(2);
        assertThat(result.expenses().get(0).getAmount()).isEqualByComparingTo("120.00");
        assertThat(result.expenses().get(1).getAmount()).isEqualByComparingTo("135.50");

        assertThat(result.balances()).hasSize(2);
        assertThat(result.balances().get(0).getSource()).isEqualTo("Salário");
        assertThat(result.balances().get(0).getAmount()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("Deve lançar BusinessException para cabeçalho fora do modelo único")
    void shouldThrowWhenHeaderDoesNotMatchSingleTemplate() {
        String wrongCsv = """
                date,category,title,amount
                2026-11-01,restaurante,iFood,64.90
                """;

        assertThatThrownBy(() -> expenseImportService.importPlanilhaCsv(
                new ByteArrayInputStream(wrongCsv.getBytes(StandardCharsets.UTF_8)),
                null
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Despesa");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao importar CSV vazio")
    void shouldThrowWhenCsvIsEmpty() {
        assertThatThrownBy(() -> expenseImportService.importPlanilhaCsv(
                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
                null
        )).isInstanceOf(BusinessException.class);
    }
}
