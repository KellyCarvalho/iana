package com.iana.domain.ports.in;

import java.util.UUID;

public interface DeleteCategoryUseCase {
    void deleteById(UUID id);
}
