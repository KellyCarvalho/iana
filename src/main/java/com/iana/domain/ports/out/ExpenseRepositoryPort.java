package com.iana.domain.ports.out;

import com.iana.domain.model.Expense;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepositoryPort {
    Expense save(Expense expense);
    Optional<Expense> findById(UUID id);
    List<Expense> findAll();
    List<Expense> findByCategoryId(UUID categoryId);
    void deleteById(UUID id);
}
