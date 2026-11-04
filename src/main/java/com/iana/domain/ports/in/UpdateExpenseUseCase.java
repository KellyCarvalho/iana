package com.iana.domain.ports.in;

import com.iana.domain.model.Expense;

import java.util.UUID;

public interface UpdateExpenseUseCase {
    Expense update(UUID id, Expense expense);
}
