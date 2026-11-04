package com.iana.application.service;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.out.ExpenseRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepositoryPort expenseRepositoryPort;

    @InjectMocks
    private ExpenseService expenseService;

    private UUID expenseId;
    private UUID categoryId;
    private Expense expense;

    @BeforeEach
    void setUp() {
        expenseId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        expense = new Expense(expenseId, "Conta de Luz", new BigDecimal("185.50"), "Boleto Enel", categoryId, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar uma despesa")
    void shouldCreateExpense() {
        when(expenseRepositoryPort.save(expense)).thenReturn(expense);

        Expense saved = expenseService.execute(expense);

        assertThat(saved.getName()).isEqualTo("Conta de Luz");
        assertThat(saved.getAmount()).isEqualByComparingTo("185.50");
        verify(expenseRepositoryPort, times(1)).save(expense);
    }

    @Test
    @DisplayName("Deve buscar despesa por ID")
    void shouldFindExpenseById() {
        when(expenseRepositoryPort.findById(expenseId)).thenReturn(Optional.of(expense));

        Optional<Expense> found = expenseService.findById(expenseId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(expenseId);
    }

    @Test
    @DisplayName("Deve listar despesas e filtrar por categoria")
    void shouldFindExpenses() {
        when(expenseRepositoryPort.findAll()).thenReturn(List.of(expense));
        when(expenseRepositoryPort.findByCategoryId(categoryId)).thenReturn(List.of(expense));

        assertThat(expenseService.findAll()).hasSize(1);
        assertThat(expenseService.findByCategoryId(categoryId)).hasSize(1);
    }

    @Test
    @DisplayName("Deve atualizar uma despesa existente")
    void shouldUpdateExpense() {
        Expense updateData = new Expense(expenseId, "Internet Fibra", new BigDecimal("120.00"), "Plano mensal", categoryId, null);
        when(expenseRepositoryPort.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseRepositoryPort.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        Expense updated = expenseService.update(expenseId, updateData);

        assertThat(updated.getName()).isEqualTo("Internet Fibra");
        assertThat(updated.getAmount()).isEqualByComparingTo("120.00");
        assertThat(updated.getDescription()).isEqualTo("Plano mensal");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar despesa inexistente")
    void shouldThrowWhenUpdatingNonExistentExpense() {
        when(expenseRepositoryPort.findById(expenseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.update(expenseId, expense))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve remover uma despesa existente")
    void shouldDeleteExpense() {
        when(expenseRepositoryPort.findById(expenseId)).thenReturn(Optional.of(expense));
        doNothing().when(expenseRepositoryPort).deleteById(expenseId);

        expenseService.deleteById(expenseId);

        verify(expenseRepositoryPort, times(1)).deleteById(expenseId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao remover despesa inexistente")
    void shouldThrowWhenDeletingNonExistentExpense() {
        when(expenseRepositoryPort.findById(expenseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteById(expenseId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
