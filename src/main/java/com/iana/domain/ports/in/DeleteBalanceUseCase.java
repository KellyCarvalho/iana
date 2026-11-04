package com.iana.domain.ports.in;

import java.util.UUID;

public interface DeleteBalanceUseCase {
    void deleteById(UUID id);
}
