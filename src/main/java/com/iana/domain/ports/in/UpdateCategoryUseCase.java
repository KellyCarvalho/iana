package com.iana.domain.ports.in;

import com.iana.domain.model.Category;

import java.util.UUID;

public interface UpdateCategoryUseCase {
    Category update(UUID id, Category category);
}
