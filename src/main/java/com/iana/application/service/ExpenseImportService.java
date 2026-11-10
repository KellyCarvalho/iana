package com.iana.application.service;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.model.Balance;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.in.ImportExpensesCsvUseCase;
import com.iana.domain.ports.out.BalanceRepositoryPort;
import com.iana.domain.ports.out.ExpenseRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ExpenseImportService implements ImportExpensesCsvUseCase {

    private static final Set<String> VALID_FIRST_HEADERS = Set.of(
            "despesa", "despesas", "descricao", "item", "conta", "contas", "gasto", "gastos", "nome"
    );

    private static final Set<String> SUMMARY_ROW_NAMES = Set.of(
            "total", "total despesas", "total de despesas", "total gastos", "total de gastos",
            "subtotal", "saldo", "saldo final", "saldo liquido", "saldo restante", "balanco", "resumo"
    );

    private final ExpenseRepositoryPort expenseRepositoryPort;
    private BalanceRepositoryPort balanceRepositoryPort;

    public ExpenseImportService(ExpenseRepositoryPort expenseRepositoryPort) {
        this.expenseRepositoryPort = expenseRepositoryPort;
    }

    @Autowired(required = false)
    public void setBalanceRepositoryPort(BalanceRepositoryPort balanceRepositoryPort) {
        this.balanceRepositoryPort = balanceRepositoryPort;
    }

    @Override
    public ImportResult importPlanilhaCsv(InputStream csvStream, UUID defaultCategoryId) {
        if (csvStream == null) {
            throw new BusinessException("Arquivo CSV não informado.");
        }

        String content = decodeCsvStream(csvStream);
        List<String> lines = Arrays.stream(content.split("\\r?\\n"))
                .map(line -> line.replace("\uFEFF", "").trim())
                .filter(line -> !line.isEmpty())
                .toList();

        if (lines.isEmpty()) {
            throw new BusinessException("O arquivo CSV está vazio.");
        }

        char delimiter = detectDelimiter(lines.get(0));
        List<String> headerCols = parseCsvLine(lines.get(0), delimiter);
        if (headerCols.size() < 2) {
            throw new BusinessException("Modelo inválido. Use o modelo único CSV com a primeira coluna 'Despesa' seguida pelas colunas dos meses.");
        }

        String firstColNormalized = normalizeText(headerCols.get(0));
        if (!VALID_FIRST_HEADERS.contains(firstColNormalized)) {
            throw new BusinessException("Modelo inválido. A primeira coluna do cabeçalho deve ser 'Despesa', seguida pelos meses (ex.: Despesa;Janeiro;Fevereiro;...) e a linha 'Salário'.");
        }

        List<Integer> monthColIndices = new ArrayList<>();
        List<String> monthNames = new ArrayList<>();
        for (int col = 1; col < headerCols.size(); col++) {
            String monthHeader = headerCols.get(col).trim();
            if (!monthHeader.isEmpty()) {
                monthColIndices.add(col);
                monthNames.add(monthHeader);
            }
        }

        if (monthColIndices.isEmpty()) {
            throw new BusinessException("Informe pelo menos uma coluna de mês após a coluna 'Despesa'.");
        }

        List<Expense> savedExpenses = new ArrayList<>();
        List<Balance> savedBalances = new ArrayList<>();

        for (int rowIdx = 1; rowIdx < lines.size(); rowIdx++) {
            List<String> cols = parseCsvLine(lines.get(rowIdx), delimiter);
            if (cols.isEmpty()) {
                continue;
            }

            String rawLabel = cols.get(0).trim();
            if (rawLabel.isEmpty()) {
                continue;
            }

            String normalizedLabel = normalizeText(rawLabel);
            if (SUMMARY_ROW_NAMES.contains(normalizedLabel)) {
                continue;
            }

            boolean salaryRow = isSalaryRow(normalizedLabel);

            for (int m = 0; m < monthColIndices.size(); m++) {
                int colIdx = monthColIndices.get(m);
                if (colIdx >= cols.size()) {
                    continue;
                }

                String cellValue = cols.get(colIdx).trim();
                if (cellValue.isEmpty() || cellValue.equals("-") || cellValue.equals("--")) {
                    continue;
                }

                BigDecimal amount = parseAmount(cellValue);
                if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                String monthName = monthNames.get(m);
                String description = truncate("Mês: " + monthName, 500);
                String label = truncate(rawLabel, 150);

                if (salaryRow) {
                    if (balanceRepositoryPort != null) {
                        Balance balance = new Balance(null, label, amount, description, LocalDateTime.now());
                        savedBalances.add(balanceRepositoryPort.save(balance));
                    }
                } else {
                    Expense expense = new Expense(null, label, amount, description, defaultCategoryId, LocalDateTime.now());
                    savedExpenses.add(expenseRepositoryPort.save(expense));
                }
            }
        }

        if (savedExpenses.isEmpty() && savedBalances.isEmpty()) {
            throw new BusinessException("Nenhum valor positivo de despesa ou salário foi encontrado na planilha CSV.");
        }

        return new ImportResult(savedExpenses, savedBalances);
    }

    private String decodeCsvStream(InputStream csvStream) {
        try {
            byte[] bytes = csvStream.readAllBytes();
            if (bytes.length == 0) {
                return "";
            }

            int offset = 0;
            if (bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xEF
                    && (bytes[1] & 0xFF) == 0xBB
                    && (bytes[2] & 0xFF) == 0xBF) {
                offset = 3;
            }

            try {
                return StandardCharsets.UTF_8.newDecoder()
                        .onMalformedInput(CodingErrorAction.REPORT)
                        .onUnmappableCharacter(CodingErrorAction.REPORT)
                        .decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset))
                        .toString();
            } catch (CharacterCodingException e) {
                return Charset.forName("windows-1252").decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset)).toString();
            }
        } catch (IOException e) {
            throw new BusinessException("Erro ao ler o arquivo CSV: " + e.getMessage());
        }
    }

    private boolean isSalaryRow(String normalizedLabel) {
        return normalizedLabel.equals("salario")
                || normalizedLabel.equals("salarios")
                || normalizedLabel.startsWith("salario ")
                || normalizedLabel.equals("renda")
                || normalizedLabel.equals("receita")
                || normalizedLabel.equals("receitas")
                || normalizedLabel.equals("entrada")
                || normalizedLabel.equals("entradas")
                || normalizedLabel.equals("proventos");
    }

    private char detectDelimiter(String headerLine) {
        long semicolons = headerLine.chars().filter(ch -> ch == ';').count();
        long commas = headerLine.chars().filter(ch -> ch == ',').count();
        long tabs = headerLine.chars().filter(ch -> ch == '\t').count();
        if (tabs > semicolons && tabs > commas) return '\t';
        return semicolons >= commas ? ';' : ',';
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

    private String normalizeText(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return normalized.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
    }

    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.replace("\u00A0", "")
                .replaceAll("[R$\\s\"']", "")
                .trim();
        if (cleaned.isEmpty()) return null;

        int lastComma = cleaned.lastIndexOf(',');
        int lastDot = cleaned.lastIndexOf('.');

        if (lastComma > lastDot) {
            cleaned = cleaned.replace(".", "").replace(",", ".");
        } else if (lastDot > lastComma && lastComma != -1) {
            cleaned = cleaned.replace(",", "");
        } else if (lastDot != -1 && cleaned.matches("^\\d{1,3}(\\.\\d{3})+$")) {
            cleaned = cleaned.replace(".", "");
        }

        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String truncate(String value, int maxLen) {
        return value.length() > maxLen ? value.substring(0, maxLen) : value;
    }
}
