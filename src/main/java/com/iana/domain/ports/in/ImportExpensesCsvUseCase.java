package com.iana.domain.ports.in;

import com.iana.domain.model.Expense;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ImportExpensesCsvUseCase {
    List<Expense> importFromCsv(InputStream csvStream, UUID defaultCategoryId);
    List<Expense> importFromPdf(byte[] pdfBytes, UUID defaultCategoryId);
}
