package com.iana.domain.ports.in;

import java.util.UUID;

public interface DeleteBookUseCase {
    void deleteById(UUID id);
}
