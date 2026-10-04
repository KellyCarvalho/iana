package com.iana.domain.ports.in;

import com.iana.domain.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindCategoryUseCase {
    Optional<Category> findById(UUID id);
    List<Category> findAll();
}
