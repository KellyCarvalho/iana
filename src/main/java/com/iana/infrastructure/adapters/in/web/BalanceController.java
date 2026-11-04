package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Balance;
import com.iana.domain.ports.in.CreateBalanceUseCase;
import com.iana.domain.ports.in.DeleteBalanceUseCase;
import com.iana.domain.ports.in.FindBalanceUseCase;
import com.iana.domain.ports.in.UpdateBalanceUseCase;
import com.iana.infrastructure.adapters.in.web.dto.BalanceResponse;
import com.iana.infrastructure.adapters.in.web.dto.CreateBalanceRequest;
import com.iana.infrastructure.adapters.in.web.dto.UpdateBalanceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/balances")
@Tag(name = "Balances", description = "Endpoints para gerenciamento de saldos e entradas")
public class BalanceController {

    private final CreateBalanceUseCase createBalanceUseCase;
    private final FindBalanceUseCase findBalanceUseCase;
    private final UpdateBalanceUseCase updateBalanceUseCase;
    private final DeleteBalanceUseCase deleteBalanceUseCase;

    public BalanceController(CreateBalanceUseCase createBalanceUseCase,
                             FindBalanceUseCase findBalanceUseCase,
                             UpdateBalanceUseCase updateBalanceUseCase,
                             DeleteBalanceUseCase deleteBalanceUseCase) {
        this.createBalanceUseCase = createBalanceUseCase;
        this.findBalanceUseCase = findBalanceUseCase;
        this.updateBalanceUseCase = updateBalanceUseCase;
        this.deleteBalanceUseCase = deleteBalanceUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar um novo saldo")
    public ResponseEntity<BalanceResponse> createBalance(@Valid @RequestBody CreateBalanceRequest request) {
        Balance balance = new Balance(null, request.getSource(), request.getAmount(), request.getDescription(), null);
        Balance created = createBalanceUseCase.execute(balance);
        return ResponseEntity.status(HttpStatus.CREATED).body(BalanceResponse.fromDomain(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar saldo por ID")
    public ResponseEntity<BalanceResponse> getBalanceById(@PathVariable UUID id) {
        return findBalanceUseCase.findById(id)
                .map(BalanceResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo não encontrado com o ID: " + id));
    }

    @GetMapping
    @Operation(summary = "Listar todos os saldos")
    public ResponseEntity<List<BalanceResponse>> getBalances() {
        List<BalanceResponse> response = findBalanceUseCase.findAll().stream()
                .map(BalanceResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um saldo existente")
    public ResponseEntity<BalanceResponse> updateBalance(@PathVariable UUID id, @Valid @RequestBody UpdateBalanceRequest request) {
        Balance balance = new Balance(id, request.getSource(), request.getAmount(), request.getDescription(), null);
        Balance updated = updateBalanceUseCase.update(id, balance);
        return ResponseEntity.ok(BalanceResponse.fromDomain(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover um saldo por ID")
    public ResponseEntity<Void> deleteBalance(@PathVariable UUID id) {
        deleteBalanceUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
