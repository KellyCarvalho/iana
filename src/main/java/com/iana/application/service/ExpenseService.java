package com.iana.application.service;

import com.iana.domain.exception.ResourceNotFoundException;
import com.iana.domain.model.Expense;
import com.iana.domain.ports.in.CreateExpenseUseCase;
import com.iana.domain.ports.in.DeleteExpenseUseCase;
import com.iana.domain.ports.in.FindExpenseUseCase;
import com.iana.domain.ports.in.UpdateExpenseUseCase;
import com.iana.domain.ports.out.ExpenseRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ExpenseService implements CreateExpenseUseCase, FindExpenseUseCase, UpdateExpenseUseCase, DeleteExpenseUseCase {

    private final ExpenseRepositoryPort expenseRepositoryPort;

    public ExpenseService(ExpenseRepositoryPort expenseRepositoryPort) {
        this.expenseRepositoryPort = expenseRepositoryPort;
    }

    @Override
    public Expense execute(Expense expense) {
        if (expense.getCreatedAt() == null) {
            expense.setCreatedAt(LocalDateTime.now());
        }
        return expenseRepositoryPort.save(expense);
    }

    @Override
    public Optional<Expense> findById(UUID id) {
        return expenseRepositoryPort.findById(id);
    }

    @Override
    public List<Expense> findAll() {
        return expenseRepositoryPort.findAll();
    }

    @Override
    public List<Expense> findByCategoryId(UUID categoryId) {
        return expenseRepositoryPort.findByCategoryId(categoryId);
    }

    @Override
    public Expense update(UUID id, Expense expense) {
        Expense existing = expenseRepositoryPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa não encontrada com o ID: " + id));

        existing.setName(expense.getName());
        existing.setAmount(expense.getAmount());
        existing.setDescription(expense.getDescription());
        existing.setCategoryId(expense.getCategoryId());

        return expenseRepositoryPort.save(existing);
    }

    @Override
    public void deleteById(UUID id) {
        if (expenseRepositoryPort.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Despesa não encontrada com o ID: " + id);
        }
        expenseRepositoryPort.deleteById(id);
    }
}
