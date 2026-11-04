package com.iana.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.in.CreateExpenseUseCase;
import com.iana.domain.ports.in.DeleteExpenseUseCase;
import com.iana.domain.ports.in.FindExpenseUseCase;
import com.iana.domain.ports.in.ImportExpensesCsvUseCase;
import com.iana.domain.ports.in.UpdateExpenseUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateExpenseRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateExpenseRequest;
import com.iana.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@Import(SecurityConfig.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateExpenseUseCase createExpenseUseCase;

    @MockitoBean
    private FindExpenseUseCase findExpenseUseCase;

    @MockitoBean
    private UpdateExpenseUseCase updateExpenseUseCase;

    @MockitoBean
    private DeleteExpenseUseCase deleteExpenseUseCase;

    @MockitoBean
    private ImportExpensesCsvUseCase importExpensesCsvUseCase;

    private UUID expenseId;
    private UUID categoryId;
    private Expense expense;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        expense = new Expense(expenseId, "Aluguel", new BigDecimal("1800.00"), "Mensalidade apartamento", categoryId, LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/v1/expenses deve criar despesa")
    void shouldCreateExpense() throws Exception {
        CreateExpenseRequest request = new CreateExpenseRequest("Aluguel", new BigDecimal("1800.00"), "Mensalidade apartamento", categoryId);
        when(createExpenseUseCase.execute(any(Expense.class))).thenReturn(expense);

        mockMvc.perform(post("/api/v1/expenses")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Aluguel"))
                .andExpect(jsonPath("$.amount").value(1800.00));
    }

    @Test
    @DisplayName("POST /api/v1/expenses/import-csv deve importar despesas via CSV")
    void shouldImportExpensesCsv() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fatura.csv",
                "text/csv",
                "date,category,title,amount\n2026-11-01,casa,Aluguel,1800.00".getBytes(StandardCharsets.UTF_8)
        );
        when(importExpensesCsvUseCase.importFromCsv(any(), any())).thenReturn(List.of(expense));

        mockMvc.perform(multipart("/api/v1/expenses/import-csv")
                        .file(file)
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].name").value("Aluguel"))
                .andExpect(jsonPath("$[0].amount").value(1800.00));
    }

    @Test
    @DisplayName("GET /api/v1/expenses/{id} deve retornar despesa por ID")
    void shouldGetExpenseById() throws Exception {
        when(findExpenseUseCase.findById(expenseId)).thenReturn(Optional.of(expense));

        mockMvc.perform(get("/api/v1/expenses/{id}", expenseId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Aluguel"));
    }

    @Test
    @DisplayName("GET /api/v1/expenses deve listar despesas")
    void shouldGetAllExpenses() throws Exception {
        when(findExpenseUseCase.findAll()).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/v1/expenses")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aluguel"));
    }

    @Test
    @DisplayName("PUT /api/v1/expenses/{id} deve atualizar despesa")
    void shouldUpdateExpense() throws Exception {
        UpdateExpenseRequest request = new UpdateExpenseRequest("Condomínio", new BigDecimal("450.00"), "Taxa mensal", categoryId);
        Expense updated = new Expense(expenseId, "Condomínio", new BigDecimal("450.00"), "Taxa mensal", categoryId, LocalDateTime.now());
        when(updateExpenseUseCase.update(eq(expenseId), any(Expense.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/expenses/{id}", expenseId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Condomínio"))
                .andExpect(jsonPath("$.amount").value(450.00));
    }

    @Test
    @DisplayName("DELETE /api/v1/expenses/{id} deve remover despesa e retornar 204")
    void shouldDeleteExpense() throws Exception {
        doNothing().when(deleteExpenseUseCase).deleteById(expenseId);

        mockMvc.perform(delete("/api/v1/expenses/{id}", expenseId)
                        .with(jwt()))
                .andExpect(status().isNoContent());
    }
}
