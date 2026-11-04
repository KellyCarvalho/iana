package com.iana.application.service;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Balance;
import com.iana.domain.ports.in.CreateBalanceUseCase;
import com.iana.domain.ports.in.DeleteBalanceUseCase;
import com.iana.domain.ports.in.FindBalanceUseCase;
import com.iana.domain.ports.in.UpdateBalanceUseCase;
import com.iana.domain.ports.out.BalanceRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BalanceService implements CreateBalanceUseCase, FindBalanceUseCase, UpdateBalanceUseCase, DeleteBalanceUseCase {

    private final BalanceRepositoryPort balanceRepositoryPort;

    public BalanceService(BalanceRepositoryPort balanceRepositoryPort) {
        this.balanceRepositoryPort = balanceRepositoryPort;
    }

    @Override
    public Balance execute(Balance balance) {
        if (balance.getCreatedAt() == null) {
            balance.setCreatedAt(LocalDateTime.now());
        }
        return balanceRepositoryPort.save(balance);
    }

    @Override
    public Optional<Balance> findById(UUID id) {
        return balanceRepositoryPort.findById(id);
    }

    @Override
    public List<Balance> findAll() {
        return balanceRepositoryPort.findAll();
    }

    @Override
    public Balance update(UUID id, Balance balance) {
        Balance existing = balanceRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Saldo não encontrado com o ID: " + id));

        existing.setSource(balance.getSource());
        existing.setAmount(balance.getAmount());
        existing.setDescription(balance.getDescription());

        return balanceRepositoryPort.save(existing);
    }

    @Override
    public void deleteById(UUID id) {
        if (balanceRepositoryPort.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Saldo não encontrado com o ID: " + id);
        }
        balanceRepositoryPort.deleteById(id);
    }
}
