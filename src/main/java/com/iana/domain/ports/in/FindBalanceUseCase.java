package com.iana.domain.ports.in;

import com.iana.domain.model.Balance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindBalanceUseCase {
    Optional<Balance> findById(UUID id);
    List<Balance> findAll();
}
