package com.iana.infrastructure.adapters.in.web;

import com.iana.domain.exception.BusinessException;
import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.in.CreateExpenseUseCase;
import com.iana.domain.ports.in.DeleteExpenseUseCase;
import com.iana.domain.ports.in.FindExpenseUseCase;
import com.iana.domain.ports.in.ImportExpensesCsvUseCase;
import com.iana.domain.ports.in.UpdateExpenseUseCase;
import com.iana.infrastructure.adapters.in.web.dto.BalanceResponse;
import com.iana.infrastructure.adapters.in.web.dto.CreateExpenseRequest;
import com.iana.infrastructure.adapters.in.web.dto.ExpenseResponse;
import com.iana.infrastructure.adapters.in.web.dto.ImportFinancesResponse;
import com.iana.infrastructure.adapters.in.web.dto.UpdateExpenseRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/expenses")
@Tag(name = "Expenses", description = "Endpoints para gerenciamento de despesas e importação da planilha modelo CSV")
public class ExpenseController {

    private final CreateExpenseUseCase createExpenseUseCase;
    private final FindExpenseUseCase findExpenseUseCase;
    private final UpdateExpenseUseCase updateExpenseUseCase;
    private final DeleteExpenseUseCase deleteExpenseUseCase;
    private ImportExpensesCsvUseCase importExpensesCsvUseCase;

    public ExpenseController(CreateExpenseUseCase createExpenseUseCase,
                             FindExpenseUseCase findExpenseUseCase,
                             UpdateExpenseUseCase updateExpenseUseCase,
                             DeleteExpenseUseCase deleteExpenseUseCase) {
        this.createExpenseUseCase = createExpenseUseCase;
        this.findExpenseUseCase = findExpenseUseCase;
        this.updateExpenseUseCase = updateExpenseUseCase;
        this.deleteExpenseUseCase = deleteExpenseUseCase;
    }

    @Autowired(required = false)
    public void setImportExpensesCsvUseCase(ImportExpensesCsvUseCase importExpensesCsvUseCase) {
        this.importExpensesCsvUseCase = importExpensesCsvUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar uma nova despesa")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        Expense expense = new Expense(null, request.getName(), request.getAmount(), request.getDescription(), request.getCategoryId(), null);
        Expense created = createExpenseUseCase.execute(expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(ExpenseResponse.fromDomain(created));
    }

    @PostMapping(value = {"/import-csv", "/import-file"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar despesas e salário a partir do modelo único em CSV (Despesa;Meses...;Salário)")
    public ResponseEntity<ImportFinancesResponse> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "categoryId", required = false) UUID categoryId) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Selecione um arquivo CSV válido para importar.");
        }
        if (importExpensesCsvUseCase == null) {
            throw new BusinessException("Serviço de importação de arquivos indisponível.");
        }
        try {
            byte[] bytes = file.getBytes();
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase(Locale.ROOT) : "";
            String contentType = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";
            boolean isPdf = filename.endsWith(".pdf")
                    || contentType.contains("pdf")
                    || (bytes.length > 4 && new String(bytes, 0, 5, StandardCharsets.ISO_8859_1).startsWith("%PDF-"));

            if (isPdf || (!filename.isEmpty() && !filename.endsWith(".csv") && !filename.endsWith(".txt"))) {
                throw new BusinessException("Formato inválido. Utilize apenas o modelo único em CSV (Despesa;Meses...;Salário).");
            }

            ImportExpensesCsvUseCase.ImportResult result =
                    importExpensesCsvUseCase.importPlanilhaCsv(new ByteArrayInputStream(bytes), categoryId);

            List<ExpenseResponse> expenses = result.expenses().stream()
                    .map(ExpenseResponse::fromDomain)
                    .collect(Collectors.toList());
            List<BalanceResponse> balances = result.balances().stream()
                    .map(BalanceResponse::fromDomain)
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(new ImportFinancesResponse(expenses, balances));
        } catch (IOException e) {
            throw new BusinessException("Falha ao processar o arquivo enviado.");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar despesa por ID")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable UUID id) {
        return findExpenseUseCase.findById(id)
                .map(ExpenseResponse::fromDomain)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada com o ID: " + id));
    }

    @GetMapping
    @Operation(summary = "Listar despesas (opcionalmente filtrando por categoryId)")
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@RequestParam(required = false) UUID categoryId) {
        List<Expense> expenses = (categoryId != null)
                ? findExpenseUseCase.findByCategoryId(categoryId)
                : findExpenseUseCase.findAll();

        List<ExpenseResponse> response = expenses.stream()
                .map(ExpenseResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma despesa existente")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable UUID id, @Valid @RequestBody UpdateExpenseRequest request) {
        Expense expense = new Expense(id, request.getName(), request.getAmount(), request.getDescription(), request.getCategoryId(), null);
        Expense updated = updateExpenseUseCase.update(id, expense);
        return ResponseEntity.ok(ExpenseResponse.fromDomain(updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover uma despesa por ID")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID id) {
        deleteExpenseUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
