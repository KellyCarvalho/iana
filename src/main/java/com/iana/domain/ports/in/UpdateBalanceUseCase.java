package com.iana.domain.ports.in;

import com.iana.domain.model.Balance;

import java.util.UUID;

public interface UpdateBalanceUseCase {
    Balance update(UUID id, Balance balance);
}
