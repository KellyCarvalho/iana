package com.iana.domain.ports.in;

import com.iana.domain.model.Expense;

public interface CreateExpenseUseCase {
    Expense execute(Expense expense);
}
