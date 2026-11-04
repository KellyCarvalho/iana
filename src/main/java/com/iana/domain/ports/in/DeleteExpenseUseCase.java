package com.iana.domain.ports.in;

import java.util.UUID;

public interface DeleteExpenseUseCase {
    void deleteById(UUID id);
}
