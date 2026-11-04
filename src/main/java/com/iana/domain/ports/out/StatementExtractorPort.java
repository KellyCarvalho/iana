package com.iana.domain.ports.out;

import com.iana.domain.model.Expense;

import java.util.List;
import java.util.UUID;

public interface StatementExtractorPort {
    List<Expense> extractFromPdf(byte[] pdfBytes, UUID defaultCategoryId);
}
