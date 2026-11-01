package com.iana.domain.ports.in;

import java.util.UUID;

public interface DeleteUserUseCase {
    void deleteById(UUID id);
}
