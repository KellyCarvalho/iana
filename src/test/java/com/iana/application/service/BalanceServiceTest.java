package com.iana.application.service;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Balance;
import com.iana.domain.ports.out.BalanceRepositoryPort;
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
class BalanceServiceTest {

    @Mock
    private BalanceRepositoryPort balanceRepositoryPort;

    @InjectMocks
    private BalanceService balanceService;

    private UUID balanceId;
    private Balance balance;

    @BeforeEach
    void setUp() {
        balanceId = UUID.randomUUID();
        balance = new Balance(balanceId, "Salário", new BigDecimal("5000.00"), "Pagamento mensal", LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar um saldo")
    void shouldCreateBalance() {
        when(balanceRepositoryPort.save(balance)).thenReturn(balance);

        Balance saved = balanceService.execute(balance);

        assertThat(saved.getSource()).isEqualTo("Salário");
        assertThat(saved.getAmount()).isEqualByComparingTo("5000.00");
        verify(balanceRepositoryPort, times(1)).save(balance);
    }

    @Test
    @DisplayName("Deve buscar saldo por ID")
    void shouldFindBalanceById() {
        when(balanceRepositoryPort.findById(balanceId)).thenReturn(Optional.of(balance));

        Optional<Balance> found = balanceService.findById(balanceId);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(balanceId);
    }

    @Test
    @DisplayName("Deve listar todos os saldos")
    void shouldFindAllBalances() {
        when(balanceRepositoryPort.findAll()).thenReturn(List.of(balance));

        assertThat(balanceService.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("Deve atualizar um saldo existente")
    void shouldUpdateBalance() {
        Balance updateData = new Balance(balanceId, "Freelance", new BigDecimal("1500.00"), "Projeto web", null);
        when(balanceRepositoryPort.findById(balanceId)).thenReturn(Optional.of(balance));
        when(balanceRepositoryPort.save(any(Balance.class))).thenAnswer(inv -> inv.getArgument(0));

        Balance updated = balanceService.update(balanceId, updateData);

        assertThat(updated.getSource()).isEqualTo("Freelance");
        assertThat(updated.getAmount()).isEqualByComparingTo("1500.00");
        assertThat(updated.getDescription()).isEqualTo("Projeto web");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao atualizar saldo inexistente")
    void shouldThrowWhenUpdatingNonExistentBalance() {
        when(balanceRepositoryPort.findById(balanceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> balanceService.update(balanceId, balance))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Deve remover um saldo existente")
    void shouldDeleteBalance() {
        when(balanceRepositoryPort.findById(balanceId)).thenReturn(Optional.of(balance));
        doNothing().when(balanceRepositoryPort).deleteById(balanceId);

        balanceService.deleteById(balanceId);

        verify(balanceRepositoryPort, times(1)).deleteById(balanceId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao remover saldo inexistente")
    void shouldThrowWhenDeletingNonExistentBalance() {
        when(balanceRepositoryPort.findById(balanceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> balanceService.deleteById(balanceId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
