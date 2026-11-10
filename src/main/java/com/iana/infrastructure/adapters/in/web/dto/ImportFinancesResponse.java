package com.iana.infrastructure.adapters.in.web.dto;

import java.util.List;

public class ImportFinancesResponse {

    private List<ExpenseResponse> expenses;
    private List<BalanceResponse> balances;
    private int totalExpensesImported;
    private int totalBalancesImported;

    public ImportFinancesResponse() {
    }

    public ImportFinancesResponse(List<ExpenseResponse> expenses, List<BalanceResponse> balances) {
        this.expenses = expenses != null ? expenses : List.of();
        this.balances = balances != null ? balances : List.of();
        this.totalExpensesImported = this.expenses.size();
        this.totalBalancesImported = this.balances.size();
    }

    public List<ExpenseResponse> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseResponse> expenses) {
        this.expenses = expenses;
        this.totalExpensesImported = expenses != null ? expenses.size() : 0;
    }

    public List<BalanceResponse> getBalances() {
        return balances;
    }

    public void setBalances(List<BalanceResponse> balances) {
        this.balances = balances;
        this.totalBalancesImported = balances != null ? balances.size() : 0;
    }

    public int getTotalExpensesImported() {
        return totalExpensesImported;
    }

    public void setTotalExpensesImported(int totalExpensesImported) {
        this.totalExpensesImported = totalExpensesImported;
    }

    public int getTotalBalancesImported() {
        return totalBalancesImported;
    }

    public void setTotalBalancesImported(int totalBalancesImported) {
        this.totalBalancesImported = totalBalancesImported;
    }
}
