package com.iana.domain.ports.in;

import com.iana.domain.model.Balance;
import com.iana.domain.model.Expense;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ImportExpensesCsvUseCase {

    record ImportResult(List<Expense> expenses, List<Balance> balances) {}

    ImportResult importPlanilhaCsv(InputStream csvStream, UUID defaultCategoryId);

    default List<Expense> importFromCsv(InputStream csvStream, UUID defaultCategoryId) {
        return importPlanilhaCsv(csvStream, defaultCategoryId).expenses();
    }
}
