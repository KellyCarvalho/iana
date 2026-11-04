package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.model.Category;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.in.ImportExpensesCsvUseCase;
import com.iana.domain.ports.out.CategoryRepositoryPort;
import com.iana.domain.ports.out.ExpenseRepositoryPort;
import com.iana.domain.ports.out.StatementExtractorPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class ExpenseImportService implements ImportExpensesCsvUseCase {

    private final ExpenseRepositoryPort expenseRepositoryPort;
    private CategoryRepositoryPort categoryRepositoryPort;
    private StatementExtractorPort statementExtractorPort;

    public ExpenseImportService(ExpenseRepositoryPort expenseRepositoryPort) {
        this.expenseRepositoryPort = expenseRepositoryPort;
    }

    @Autowired(required = false)
    public void setCategoryRepositoryPort(CategoryRepositoryPort categoryRepositoryPort) {
        this.categoryRepositoryPort = categoryRepositoryPort;
    }

    @Autowired(required = false)
    public void setStatementExtractorPort(StatementExtractorPort statementExtractorPort) {
        this.statementExtractorPort = statementExtractorPort;
    }

    @Override
    public List<Expense> importFromPdf(byte[] pdfBytes, UUID defaultCategoryId) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new BusinessException("O arquivo PDF está vazio.");
        }
        if (statementExtractorPort == null) {
            throw new BusinessException("Extrator de PDF não configurado.");
        }

        List<Expense> extracted = statementExtractorPort.extractFromPdf(pdfBytes, defaultCategoryId);
        if (extracted.isEmpty()) {
            throw new BusinessException("Não foi possível identificar lançamentos ou boleto válido neste PDF.");
        }

        List<Expense> saved = new ArrayList<>();
        for (Expense expense : extracted) {
            if (expense.getCreatedAt() == null) {
                expense.setCreatedAt(LocalDateTime.now());
            }
            saved.add(expenseRepositoryPort.save(expense));
        }
        return saved;
    }

    @Override
    public List<Expense> importFromCsv(InputStream csvStream, UUID defaultCategoryId) {
        if (csvStream == null) {
            throw new BusinessException("Arquivo CSV não informado.");
        }

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.replace("\uFEFF", "").trim();
                if (!trimmed.isEmpty()) {
                    lines.add(trimmed);
                }
            }
        } catch (IOException e) {
            throw new BusinessException("Erro ao ler o arquivo CSV: " + e.getMessage());
        }

        if (lines.isEmpty()) {
            throw new BusinessException("O arquivo CSV está vazio.");
        }

        char delimiter = detectDelimiter(lines.get(0));
        List<String> firstRowCols = parseCsvLine(lines.get(0), delimiter);

        int nameIdx = -1;
        int amountIdx = -1;
        int descIdx = -1;
        int dateIdx = -1;
        int categoryIdx = -1;
        boolean hasHeader = false;

        for (int i = 0; i < firstRowCols.size(); i++) {
            String col = normalizeHeader(firstRowCols.get(i));
            if (nameIdx == -1 && (col.contains("title") || col.contains("titulo") || col.contains("nome") || col.contains("name")
                    || col.contains("estabelecimento") || col.contains("lancamento") || col.contains("historico") || col.contains("comercio")
                    || col.contains("merchant") || col.contains("credor") || col.contains("beneficiario"))) {
                nameIdx = i;
                hasHeader = true;
            } else if (amountIdx == -1 && (col.contains("amount") || col.contains("valor") || col.contains("value")
                    || col.contains("preco") || col.contains("quantia") || col.contains("total") || col.contains("reais"))) {
                amountIdx = i;
                hasHeader = true;
            } else if (descIdx == -1 && (col.contains("description") || col.contains("descricao") || col.contains("detalhes")
                    || col.contains("observacao") || col.contains("obs") || col.contains("parcela"))) {
                descIdx = i;
                hasHeader = true;
            } else if (dateIdx == -1 && (col.contains("date") || col.contains("data") || col.contains("vencimento") || col.contains("dia"))) {
                dateIdx = i;
                hasHeader = true;
            } else if (categoryIdx == -1 && (col.contains("category") || col.contains("categoria") || col.contains("tipo") || col.contains("tag"))) {
                categoryIdx = i;
                hasHeader = true;
            }
        }

        List<String> sampleDataRow = (hasHeader && lines.size() > 1)
                ? parseCsvLine(lines.get(1), delimiter)
                : firstRowCols;

        if (!hasHeader) {
            boolean firstRowHasNumber = firstRowCols.stream().anyMatch(c -> parseAmount(c) != null);
            if (!firstRowHasNumber && lines.size() > 1) {
                hasHeader = true;
                sampleDataRow = parseCsvLine(lines.get(1), delimiter);
            }
        }

        if (amountIdx == -1 || nameIdx == -1) {
            for (int i = 0; i < sampleDataRow.size(); i++) {
                String cell = sampleDataRow.get(i).trim();
                if (dateIdx == -1 && looksLikeDate(cell)) {
                    dateIdx = i;
                } else if (amountIdx == -1 && !looksLikeDate(cell) && parseAmount(cell) != null) {
                    amountIdx = i;
                } else if (nameIdx == -1 && !cell.isEmpty() && parseAmount(cell) == null && !looksLikeDate(cell)) {
                    nameIdx = i;
                } else if (descIdx == -1 && i != nameIdx && !cell.isEmpty() && parseAmount(cell) == null) {
                    descIdx = i;
                }
            }
        }

        if (nameIdx == -1 && descIdx != -1) {
            nameIdx = descIdx;
        }

        Map<String, UUID> categoryNameMap = new HashMap<>();
        if (categoryRepositoryPort != null) {
            for (Category c : categoryRepositoryPort.findAll()) {
                if (c.getName() != null) {
                    categoryNameMap.put(normalizeHeader(c.getName()), c.getId());
                }
            }
        }

        int startRow = hasHeader ? 1 : 0;
        List<Expense> savedExpenses = new ArrayList<>();

        for (int i = startRow; i < lines.size(); i++) {
            List<String> cols = parseCsvLine(lines.get(i), delimiter);
            if (cols.isEmpty() || nameIdx < 0 || amountIdx < 0 || nameIdx >= cols.size() || amountIdx >= cols.size()) {
                continue;
            }

            String rawName = cols.get(nameIdx).trim();
            BigDecimal amount = parseAmount(cols.get(amountIdx));

            if (rawName.isEmpty() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            String normalizedName = normalizeHeader(rawName);
            if (normalizedName.contains("pagamento recebido") || normalizedName.contains("pagamento da fatura")) {
                continue;
            }

            String dateVal = (dateIdx >= 0 && dateIdx < cols.size()) ? cols.get(dateIdx).trim() : "";
            String catVal = (categoryIdx >= 0 && categoryIdx < cols.size()) ? cols.get(categoryIdx).trim() : "";
            String rawDesc = (descIdx >= 0 && descIdx < cols.size() && descIdx != nameIdx) ? cols.get(descIdx).trim() : "";

            StringBuilder descBuilder = new StringBuilder();
            if (!rawDesc.isEmpty()) {
                descBuilder.append(rawDesc);
            }
            if (!dateVal.isEmpty()) {
                if (descBuilder.length() > 0) descBuilder.append(" | ");
                descBuilder.append("Data: ").append(dateVal);
            }
            if (!catVal.isEmpty()) {
                if (descBuilder.length() > 0) descBuilder.append(" | ");
                descBuilder.append("Origem/Cat: ").append(catVal);
            }
            if (descBuilder.length() == 0) {
                descBuilder.append("Importado via fatura CSV");
            }

            UUID resolvedCategoryId = defaultCategoryId;
            if (resolvedCategoryId == null && !catVal.isEmpty()) {
                resolvedCategoryId = categoryNameMap.get(normalizeHeader(catVal));
            }

            String finalName = rawName.length() > 150 ? rawName.substring(0, 150) : rawName;
            String finalDesc = descBuilder.length() > 500 ? descBuilder.substring(0, 500) : descBuilder.toString();

            Expense expense = new Expense(null, finalName, amount, finalDesc, resolvedCategoryId, LocalDateTime.now());
            savedExpenses.add(expenseRepositoryPort.save(expense));
        }

        if (savedExpenses.isEmpty()) {
            throw new BusinessException("Nenhuma despesa válida com valor positivo foi encontrada no CSV.");
        }

        return savedExpenses;
    }

    private boolean looksLikeDate(String value) {
        if (value == null) return false;
        String v = value.trim();
        return v.matches("^\\d{2}/\\d{2}(/\\d{2,4})?$")
                || v.matches("^\\d{4}-\\d{2}-\\d{2}.*")
                || v.matches("(?i)^\\d{1,2}\\s+(jan|fev|mar|abr|mai|jun|jul|ago|set|out|nov|dez).*");
    }

    private char detectDelimiter(String headerLine) {
        long semicolons = headerLine.chars().filter(ch -> ch == ';').count();
        long commas = headerLine.chars().filter(ch -> ch == ',').count();
        long tabs = headerLine.chars().filter(ch -> ch == '\t').count();
        if (tabs > semicolons && tabs > commas) return '\t';
        return semicolons > commas ? ';' : ',';
    }

    private List<String> parseCsvLine(String line, char delimiter) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == delimiter && !inQuotes) {
                tokens.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString().trim());
        return tokens;
    }

    private String normalizeHeader(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.replaceAll("[R$\\s\"']", "").trim();
        if (cleaned.isEmpty()) return null;

        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');

        if (lastComma > lastDot) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else if (lastDot > lastComma && lastComma != -1) {
            cleaned = cleaned.replace(",", "");
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
