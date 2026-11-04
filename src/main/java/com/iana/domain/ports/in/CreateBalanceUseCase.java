package com.iana.domain.ports.in;

import com.iana.domain.model.Balance;

public interface CreateBalanceUseCase {
    Balance execute(Balance balance);
}
