package com.iana.domain.ports.out;

import com.iana.domain.model.Balance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BalanceRepositoryPort {
    Balance save(Balance balance);
    Optional<Balance> findById(UUID id);
    List<Balance> findAll();
    void deleteById(UUID id);
}
