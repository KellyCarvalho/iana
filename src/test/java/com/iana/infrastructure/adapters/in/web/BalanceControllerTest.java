package com.iana.infrastructure.adapters.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iana.domain.model.Balance;
import com.iana.domain.ports.in.CreateBalanceUseCase;
import com.iana.domain.ports.in.DeleteBalanceUseCase;
import com.iana.domain.ports.in.FindBalanceUseCase;
import com.iana.domain.ports.in.UpdateBalanceUseCase;
import com.iana.infrastructure.adapters.in.web.dto.CreateBalanceRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateBalanceRequest;
import com.iana.infrastructure.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(BalanceController.class)
@Import(SecurityConfig.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateBalanceUseCase createBalanceUseCase;

    @MockitoBean
    private FindBalanceUseCase findBalanceUseCase;

    @MockitoBean
    private UpdateBalanceUseCase updateBalanceUseCase;

    @MockitoBean
    private DeleteBalanceUseCase deleteBalanceUseCase;

    private UUID balanceId;
    private Balance balance;

    @BeforeEach
    void setUp() {
        balanceId = UUID.randomUUID();
        balance = new Balance(balanceId, "Salário", new BigDecimal("4200.00"), "Salário líquido", LocalDateTime.now());
    }

    @Test
    @DisplayName("POST /api/v1/balances deve criar saldo")
    void shouldCreateBalance() throws Exception {
        CreateBalanceRequest request = new CreateBalanceRequest("Salário", new BigDecimal("4200.00"), "Salário líquido");
        when(createBalanceUseCase.execute(any(Balance.class))).thenReturn(balance);

        mockMvc.perform(post("/api/v1/balances")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("Salário"))
                .andExpect(jsonPath("$.amount").value(4200.00));
    }

    @Test
    @DisplayName("GET /api/v1/balances/{id} deve retornar saldo por ID")
    void shouldGetBalanceById() throws Exception {
        when(findBalanceUseCase.findById(balanceId)).thenReturn(Optional.of(balance));

        mockMvc.perform(get("/api/v1/balances/{id}", balanceId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("Salário"));
    }

    @Test
    @DisplayName("GET /api/v1/balances deve listar saldos")
    void shouldGetAllBalances() throws Exception {
        when(findBalanceUseCase.findAll()).thenReturn(List.of(balance));

        mockMvc.perform(get("/api/v1/balances")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source").value("Salário"));
    }

    @Test
    @DisplayName("PUT /api/v1/balances/{id} deve atualizar saldo")
    void shouldUpdateBalance() throws Exception {
        UpdateBalanceRequest request = new UpdateBalanceRequest("Reserva", new BigDecimal("6000.00"), "Poupança");
        Balance updated = new Balance(balanceId, "Reserva", new BigDecimal("6000.00"), "Poupança", LocalDateTime.now());
        when(updateBalanceUseCase.update(eq(balanceId), any(Balance.class))).thenReturn(updated);

        mockMvc.perform(put("/api/v1/balances/{id}", balanceId)
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("Reserva"))
                .andExpect(jsonPath("$.amount").value(6000.00));
    }

    @Test
    @DisplayName("DELETE /api/v1/balances/{id} deve remover saldo e retornar 204")
    void shouldDeleteBalance() throws Exception {
        doNothing().when(deleteBalanceUseCase).deleteById(balanceId);

        mockMvc.perform(delete("/api/v1/balances/{id}", balanceId)
                        .with(jwt()))
                .andExpect(status().isNoContent());
    }
}
