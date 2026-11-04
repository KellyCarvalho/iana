package com.iana.domain.ports.in;

import com.iana.domain.model.Expense;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FindExpenseUseCase {
    Optional<Expense> findById(UUID id);
    List<Expense> findAll();
    List<Expense> findByCategoryId(UUID categoryId);
}
