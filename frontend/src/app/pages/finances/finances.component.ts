import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { ExpenseService } from '../../core/services/expense.service';
import { BalanceService } from '../../core/services/balance.service';
import { Balance, Category, Expense } from '../../core/models/api.models';

@Component({
  selector: 'app-finances',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <div>
        <h1>Organizador de Finanças</h1>
        <p style="color: #475569; font-size: 0.9rem; margin-top: 0.2rem;">
          Gerencie suas despesas, importe sua planilha no modelo único CSV e acompanhe seu saldo.
        </p>
      </div>
      <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
        <button
          type="button"
          class="btn"
          [ngClass]="activeTab === 'all' ? 'btn-primary' : 'btn-secondary'"
          (click)="activeTab = 'all'">
          Visão Geral
        </button>
        <button
          type="button"
          class="btn"
          [ngClass]="activeTab === 'expenses' ? 'btn-primary' : 'btn-secondary'"
          (click)="activeTab = 'expenses'">
          Despesas ({{ expenses.length }})
        </button>
        <button
          type="button"
          class="btn"
          [ngClass]="activeTab === 'balances' ? 'btn-primary' : 'btn-secondary'"
          (click)="activeTab = 'balances'">
          Saldos ({{ balances.length }})
        </button>
      </div>
    </div>

    <div *ngIf="errorMessage" class="alert alert-error">{{ errorMessage }}</div>
    <div *ngIf="successMessage" class="alert alert-success">{{ successMessage }}</div>

    <!-- Cards de Resumo Financeiro -->
    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
      <div class="card" style="margin-bottom: 0; border-left: 5px solid #059669;">
        <div style="font-size: 0.78rem; color: #475569; text-transform: uppercase; font-weight: 700;">
          Total de Saldos / Entradas
        </div>
        <div style="font-size: 1.65rem; font-weight: 800; color: #059669; margin-top: 0.35rem;">
          {{ formatCurrency(totalBalances) }}
        </div>
        <div style="font-size: 0.78rem; color: #64748b; margin-top: 0.25rem;">
          {{ balances.length }} registro(s) de saldo
        </div>
      </div>

      <div class="card" style="margin-bottom: 0; border-left: 5px solid #dc2626;">
        <div style="font-size: 0.78rem; color: #475569; text-transform: uppercase; font-weight: 700;">
          Total de Despesas
        </div>
        <div style="font-size: 1.65rem; font-weight: 800; color: #dc2626; margin-top: 0.35rem;">
          {{ formatCurrency(totalExpenses) }}
        </div>
        <div style="font-size: 0.78rem; color: #64748b; margin-top: 0.25rem;">
          {{ expenses.length }} despesa(s) lançada(s)
        </div>
      </div>

      <div
        class="card"
        style="margin-bottom: 0;"
        [style.border-left]="netBalance >= 0 ? '5px solid #2563eb' : '5px solid #d97706'">
        <div style="font-size: 0.78rem; color: #475569; text-transform: uppercase; font-weight: 700;">
          Saldo Líquido (Saldos - Despesas)
        </div>
        <div
          style="font-size: 1.65rem; font-weight: 800; margin-top: 0.35rem;"
          [style.color]="netBalance >= 0 ? '#1d4ed8' : '#d97706'">
          {{ formatCurrency(netBalance) }}
        </div>
        <div style="font-size: 0.78rem; color: #64748b; margin-top: 0.25rem;">
          {{ balances.length === 0 ? 'Cadastre um saldo (opcional) para abater' : (netBalance >= 0 ? 'Dentro do orçamento' : 'Despesas superam o saldo informado') }}
        </div>
      </div>
    </div>

    <!-- SEÇÃO DE DESPESAS -->
    <ng-container *ngIf="activeTab === 'all' || activeTab === 'expenses'">
      <!-- Card de Importação de Planilha (Modelo Único CSV) -->
      <div class="card" style="border: 2px dashed #60a5fa; background: linear-gradient(135deg, #ffffff 0%, #eff6ff 100%);">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 0.85rem;">
          <div>
            <h2 style="font-size: 1.1rem; color: #1e3a8a; display: flex; align-items: center; gap: 0.5rem;">
              📊 Importar Planilha (Modelo Único CSV)
            </h2>
            <p style="font-size: 0.84rem; color: #475569; margin-top: 0.25rem;">
              Utilize o modelo único com a coluna <strong>Despesa</strong>, as colunas dos <strong>meses</strong> (Janeiro, Fevereiro...) e a linha <strong>Salário</strong> para suas entradas.
            </p>
          </div>
          <button type="button" class="btn btn-secondary btn-sm" (click)="downloadSampleCsv()">
            📥 Baixar Modelo CSV
          </button>
        </div>

        <div class="form-grid" style="align-items: end;">
          <div class="form-group">
            <label>Arquivo da Planilha (.CSV) *</label>
            <input
              #csvFileInput
              type="file"
              accept=".csv,text/csv"
              (change)="onCsvFileSelected($event)"
              style="padding: 0.5rem; background: #ffffff;" />
          </div>
          <div class="form-group">
            <label>Categoria para as Despesas (opcional)</label>
            <select [(ngModel)]="csvCategoryId" name="csvCategoryId" style="background: #ffffff;">
              <option [ngValue]="undefined">Sem categoria</option>
              <option *ngFor="let c of categories" [value]="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="actions" style="margin-bottom: 0.15rem;">
            <button
              type="button"
              class="btn btn-primary"
              [disabled]="!selectedCsvFile || csvPreviewItems.length === 0 || isImportingCsv"
              (click)="uploadCsv()">
              {{ isImportingCsv ? 'Importando...' : 'Importar Planilha' }}
            </button>
            <button
              *ngIf="selectedCsvFile"
              type="button"
              class="btn btn-secondary"
              (click)="clearCsvSelection()">
              Limpar
            </button>
          </div>
        </div>

        <!-- Preview rápido do CSV selecionado -->
        <div *ngIf="csvPreviewItems.length > 0" style="margin-top: 1rem; padding-top: 0.85rem; border-top: 1px solid #bfdbfe;">
          <div style="font-size: 0.82rem; color: #1e3a8a; margin-bottom: 0.5rem; font-weight: 600;">
            Pré-visualização ({{ csvPreviewItems.length }} lançamento(s) identificado(s)):
          </div>
          <div style="max-height: 200px; overflow-y: auto; border: 1px solid #bfdbfe; border-radius: 8px; background: #ffffff;">
            <table>
              <thead>
                <tr>
                  <th>Tipo</th>
                  <th>Despesa / Origem</th>
                  <th>Mês</th>
                  <th>Valor</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of csvPreviewItems">
                  <td style="padding: 0.5rem 0.85rem;">
                    <span
                      class="badge"
                      [ngClass]="item.type === 'balance' ? 'badge-success' : 'badge-danger'">
                      {{ item.type === 'balance' ? 'Salário' : 'Despesa' }}
                    </span>
                  </td>
                  <td style="padding: 0.5rem 0.85rem; font-weight: 500;">{{ item.name }}</td>
                  <td style="padding: 0.5rem 0.85rem; color: #475569; font-size: 0.82rem;">{{ item.month }}</td>
                  <td
                    style="padding: 0.5rem 0.85rem; font-weight: 600;"
                    [style.color]="item.type === 'balance' ? '#059669' : '#dc2626'">
                    {{ item.type === 'balance' ? '+' : '-' }} {{ formatCurrency(item.amount) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Formulário manual de Despesa -->
      <div class="card">
        <h2 style="font-size: 1.15rem; margin-bottom: 1rem; color: #1e3a8a;">
          {{ editingExpenseId ? 'Editar Despesa' : 'Nova Despesa Manual' }}
        </h2>
        <form (ngSubmit)="saveExpense()">
          <div class="form-grid">
            <div class="form-group">
              <label>Nome da Despesa *</label>
              <input
                type="text"
                [(ngModel)]="expenseForm.name"
                name="expenseName"
                placeholder="Ex: Conta de Luz, Supermercado..."
                required />
            </div>
            <div class="form-group">
              <label>Valor (R$) *</label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                [(ngModel)]="expenseForm.amount"
                name="expenseAmount"
                placeholder="Ex: 150.00"
                required />
            </div>
            <div class="form-group">
              <label>Categoria (opcional)</label>
              <select [(ngModel)]="expenseForm.categoryId" name="expenseCategoryId">
                <option [ngValue]="undefined">Sem categoria</option>
                <option *ngFor="let c of categories" [value]="c.id">{{ c.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>Descrição *</label>
              <input
                type="text"
                [(ngModel)]="expenseForm.description"
                name="expenseDescription"
                placeholder="Ex: Referente ao mês atual / boleto vencimento dia 10"
                required />
            </div>
          </div>
          <div class="actions">
            <button type="submit" class="btn btn-primary">
              {{ editingExpenseId ? 'Salvar Alterações' : 'Adicionar Despesa' }}
            </button>
            <button *ngIf="editingExpenseId" type="button" class="btn btn-secondary" (click)="resetExpenseForm()">
              Cancelar
            </button>
          </div>
        </form>
      </div>

      <div class="card">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem; margin-bottom: 1rem;">
          <h2 style="font-size: 1.1rem; color: #1e3a8a;">Despesas Cadastradas</h2>
          <div style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
            <select
              [(ngModel)]="selectedCategoryFilter"
              (ngModelChange)="onCategoryFilterChange()"
              style="padding: 0.45rem 0.75rem; font-size: 0.85rem;">
              <option value="">Todas as categorias</option>
              <option *ngFor="let c of categories" [value]="c.id">{{ c.name }}</option>
            </select>
          </div>
        </div>

        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>Descrição</th>
              <th>Categoria</th>
              <th>Valor</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let e of expenses">
              <td style="font-weight: 600;">{{ e.name }}</td>
              <td style="color: #475569;">{{ e.description || '-' }}</td>
              <td>
                <span class="badge badge-user">{{ getCategoryName(e.categoryId) }}</span>
              </td>
              <td style="font-weight: 700; color: #dc2626;">
                - {{ formatCurrency(e.amount) }}
              </td>
              <td class="actions">
                <button class="btn btn-secondary btn-sm" (click)="editExpense(e)">Editar</button>
                <button class="btn btn-danger btn-sm" (click)="requestDeleteExpense(e)">Excluir</button>
              </td>
            </tr>
            <tr *ngIf="expenses.length === 0">
              <td colspan="5" style="text-align: center; color: #64748b;">Nenhuma despesa cadastrada.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </ng-container>

    <!-- SEÇÃO DE SALDOS (OPCIONAL) -->
    <ng-container *ngIf="activeTab === 'all' || activeTab === 'balances'">
      <div class="card">
        <h2 style="font-size: 1.15rem; margin-bottom: 1rem; color: #1e3a8a;">
          {{ editingBalanceId ? 'Editar Saldo' : 'Novo Saldo (Opcional)' }}
        </h2>
        <form (ngSubmit)="saveBalance()">
          <div class="form-grid">
            <div class="form-group">
              <label>Origem *</label>
              <input
                type="text"
                [(ngModel)]="balanceForm.source"
                name="balanceSource"
                placeholder="Ex: Salário, Reserva, Freelance..."
                required />
            </div>
            <div class="form-group">
              <label>Valor (R$) *</label>
              <input
                type="number"
                step="0.01"
                min="0.01"
                [(ngModel)]="balanceForm.amount"
                name="balanceAmount"
                placeholder="Ex: 3500.00"
                required />
            </div>
            <div class="form-group">
              <label>Descrição (opcional)</label>
              <input
                type="text"
                [(ngModel)]="balanceForm.description"
                name="balanceDescription"
                placeholder="Observação opcional sobre este saldo" />
            </div>
          </div>
          <div class="actions">
            <button type="submit" class="btn btn-primary">
              {{ editingBalanceId ? 'Salvar Alterações' : 'Adicionar Saldo' }}
            </button>
            <button *ngIf="editingBalanceId" type="button" class="btn btn-secondary" (click)="resetBalanceForm()">
              Cancelar
            </button>
          </div>
        </form>
      </div>

      <div class="card">
        <h2 style="font-size: 1.1rem; margin-bottom: 1rem; color: #1e3a8a;">Saldos / Entradas Cadastradas</h2>
        <table>
          <thead>
            <tr>
              <th>Origem</th>
              <th>Descrição</th>
              <th>Valor</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let b of balances">
              <td style="font-weight: 600;">{{ b.source }}</td>
              <td style="color: #475569;">{{ b.description || '-' }}</td>
              <td style="font-weight: 700; color: #059669;">
                + {{ formatCurrency(b.amount) }}
              </td>
              <td class="actions">
                <button class="btn btn-secondary btn-sm" (click)="editBalance(b)">Editar</button>
                <button class="btn btn-danger btn-sm" (click)="requestDeleteBalance(b)">Excluir</button>
              </td>
            </tr>
            <tr *ngIf="balances.length === 0">
              <td colspan="4" style="text-align: center; color: #64748b;">Nenhum saldo cadastrado ainda.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </ng-container>

    <!-- Modal de confirmação de exclusão de Despesa -->
    <div
      *ngIf="expenseToDelete"
      style="position: fixed; inset: 0; background: rgba(15, 23, 42, 0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;">
      <div class="card" style="max-width: 440px; width: 100%; margin: 0; border: 1px solid #bfdbfe;">
        <h3 style="margin-bottom: 0.75rem; font-size: 1.15rem; color: #1e3a8a;">Confirmar Exclusão de Despesa</h3>
        <p style="color: #334155; margin-bottom: 1.25rem; line-height: 1.5;">
          Tem certeza que deseja excluir a despesa <strong>"{{ expenseToDelete.name }}"</strong> no valor de
          <strong>{{ formatCurrency(expenseToDelete.amount) }}</strong>?
        </p>
        <div class="actions" style="justify-content: flex-end;">
          <button type="button" class="btn btn-secondary" (click)="expenseToDelete = null">Cancelar</button>
          <button type="button" class="btn btn-danger" (click)="confirmDeleteExpense()">Confirmar Exclusão</button>
        </div>
      </div>
    </div>

    <!-- Modal de confirmação de exclusão de Saldo -->
    <div
      *ngIf="balanceToDelete"
      style="position: fixed; inset: 0; background: rgba(15, 23, 42, 0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;">
      <div class="card" style="max-width: 440px; width: 100%; margin: 0; border: 1px solid #bfdbfe;">
        <h3 style="margin-bottom: 0.75rem; font-size: 1.15rem; color: #1e3a8a;">Confirmar Exclusão de Saldo</h3>
        <p style="color: #334155; margin-bottom: 1.25rem; line-height: 1.5;">
          Tem certeza que deseja excluir o saldo de origem <strong>"{{ balanceToDelete.source }}"</strong> no valor de
          <strong>{{ formatCurrency(balanceToDelete.amount) }}</strong>?
        </p>
        <div class="actions" style="justify-content: flex-end;">
          <button type="button" class="btn btn-secondary" (click)="balanceToDelete = null">Cancelar</button>
          <button type="button" class="btn btn-danger" (click)="confirmDeleteBalance()">Confirmar Exclusão</button>
        </div>
      </div>
    </div>
  `
})
export class FinancesComponent implements OnInit {
  expenses: Expense[] = [];
  balances: Balance[] = [];
  categories: Category[] = [];

  activeTab: 'all' | 'expenses' | 'balances' = 'all';
  selectedCategoryFilter = '';

  editingExpenseId: string | null = null;
  editingBalanceId: string | null = null;

  expenseToDelete: Expense | null = null;
  balanceToDelete: Balance | null = null;

  @ViewChild('csvFileInput') csvFileInput?: ElementRef<HTMLInputElement>;

  selectedCsvFile: File | null = null;
  csvCategoryId?: string = undefined;
  isImportingCsv = false;
  csvPreviewItems: Array<{ type: 'expense' | 'balance'; name: string; month: string; amount: number }> = [];

  errorMessage = '';
  successMessage = '';

  expenseForm: {
    name: string;
    amount: number | null;
    description: string;
    categoryId?: string;
  } = { name: '', amount: null, description: '', categoryId: undefined };

  balanceForm: {
    source: string;
    amount: number | null;
    description: string;
  } = { source: '', amount: null, description: '' };

  constructor(
    private expenseService: ExpenseService,
    private balanceService: BalanceService,
    private api: ApiService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const routeTab = this.route.snapshot.data?.['tab'];
    if (routeTab === 'expenses' || routeTab === 'balances') {
      this.activeTab = routeTab;
    }
    this.loadAll();
  }

  loadAll(): void {
    this.errorMessage = '';
    this.loadExpenses();
    this.loadBalances();
    this.api.getCategories().subscribe({
      next: (res) => (this.categories = res || [])
    });
  }

  loadExpenses(): void {
    const categoryId = this.selectedCategoryFilter || undefined;
    this.expenseService.getExpenses(categoryId).subscribe({
      next: (res) => (this.expenses = res || []),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erro ao carregar despesas.';
      }
    });
  }

  loadBalances(): void {
    this.balanceService.getBalances().subscribe({
      next: (res) => (this.balances = res || []),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erro ao carregar saldos.';
      }
    });
  }

  onCategoryFilterChange(): void {
    this.loadExpenses();
  }

  get totalExpenses(): number {
    return this.expenses.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
  }

  get totalBalances(): number {
    return this.balances.reduce((sum, item) => sum + (Number(item.amount) || 0), 0);
  }

  get netBalance(): number {
    return this.totalBalances - this.totalExpenses;
  }

  formatCurrency(value: number | null | undefined): string {
    const num = Number(value) || 0;
    return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
  }

  getCategoryName(categoryId?: string): string {
    if (!categoryId) return 'Sem categoria';
    return this.categories.find((c) => c.id === categoryId)?.name || 'Sem categoria';
  }

  // Importação da planilha modelo único CSV (Despesa | Meses... | Salário)
  onCsvFileSelected(event: Event): void {
    this.errorMessage = '';
    this.successMessage = '';
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      this.clearCsvSelection();
      return;
    }
    const file = input.files[0];
    if (!file.name.toLowerCase().endsWith('.csv') && !file.name.toLowerCase().endsWith('.txt')) {
      this.clearCsvSelection();
      this.errorMessage = 'Formato inválido. Selecione um arquivo .csv no modelo único (Despesa;Meses...;Salário).';
      return;
    }
    this.selectedCsvFile = file;
    this.buildClientSideCsvPreview(file);
  }

  private buildClientSideCsvPreview(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      const buffer = reader.result as ArrayBuffer;
      const text = this.decodeCsvBuffer(buffer);
      const lines = text
        .replace(/\uFEFF/g, '')
        .split(/\r?\n/)
        .map((l) => l.trim())
        .filter((l) => l.length > 0);

      if (lines.length === 0) {
        this.csvPreviewItems = [];
        this.errorMessage = 'O arquivo CSV está vazio.';
        return;
      }

      const delimiter = this.detectCsvDelimiter(lines[0]);
      const headerCols = this.parseCsvLine(lines[0], delimiter);
      const firstHeader = this.normalizeText(headerCols[0] || '');
      const validHeaders = ['despesa', 'despesas', 'descricao', 'item', 'conta', 'contas', 'gasto', 'gastos', 'nome'];

      if (headerCols.length < 2 || !validHeaders.includes(firstHeader)) {
        this.csvPreviewItems = [];
        this.errorMessage =
          "Modelo inválido. A primeira coluna do cabeçalho deve ser 'Despesa' seguida pelos meses (ex.: Despesa;Janeiro;Fevereiro;...) e a linha 'Salário'.";
        return;
      }

      const monthColumns: Array<{ index: number; name: string }> = [];
      for (let c = 1; c < headerCols.length; c++) {
        const name = headerCols[c].trim();
        if (name) {
          monthColumns.push({ index: c, name });
        }
      }

      if (monthColumns.length === 0) {
        this.csvPreviewItems = [];
        this.errorMessage = "Informe pelo menos uma coluna de mês após a coluna 'Despesa'.";
        return;
      }

      const summaryRows = new Set([
        'total', 'total despesas', 'total de despesas', 'total gastos', 'total de gastos',
        'subtotal', 'saldo', 'saldo final', 'saldo liquido', 'saldo restante', 'balanco', 'resumo'
      ]);

      const preview: Array<{ type: 'expense' | 'balance'; name: string; month: string; amount: number }> = [];

      for (let i = 1; i < lines.length; i++) {
        const cols = this.parseCsvLine(lines[i], delimiter);
        const rawLabel = (cols[0] || '').trim();
        if (!rawLabel) continue;

        const normalizedLabel = this.normalizeText(rawLabel);
        if (summaryRows.has(normalizedLabel)) continue;

        const isSalary =
          normalizedLabel === 'salario' ||
          normalizedLabel === 'salarios' ||
          normalizedLabel.startsWith('salario ') ||
          normalizedLabel === 'renda' ||
          normalizedLabel === 'receita' ||
          normalizedLabel === 'receitas' ||
          normalizedLabel === 'entrada' ||
          normalizedLabel === 'entradas' ||
          normalizedLabel === 'proventos';

        for (const monthCol of monthColumns) {
          const cell = (cols[monthCol.index] || '').trim();
          if (!cell || cell === '-' || cell === '--') continue;

          const amount = this.parseCsvAmount(cell);
          if (amount === null || amount <= 0) continue;

          preview.push({
            type: isSalary ? 'balance' : 'expense',
            name: rawLabel,
            month: monthCol.name,
            amount
          });
        }
      }

      this.csvPreviewItems = preview;
      if (preview.length === 0) {
        this.errorMessage = 'Nenhum valor positivo de despesa ou salário foi encontrado na planilha.';
      }
    };
    reader.readAsArrayBuffer(file);
  }

  private decodeCsvBuffer(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    try {
      return new TextDecoder('utf-8', { fatal: true }).decode(bytes);
    } catch {
      return new TextDecoder('windows-1252').decode(bytes);
    }
  }

  private detectCsvDelimiter(headerLine: string): string {
    const semicolons = (headerLine.match(/;/g) || []).length;
    const commas = (headerLine.match(/,/g) || []).length;
    const tabs = (headerLine.match(/\t/g) || []).length;
    if (tabs > semicolons && tabs > commas) return '\t';
    return semicolons >= commas ? ';' : ',';
  }

  private parseCsvLine(line: string, delimiter: string): string[] {
    const tokens: string[] = [];
    let current = '';
    let inQuotes = false;
    for (let i = 0; i < line.length; i++) {
      const ch = line[i];
      if (ch === '"') {
        inQuotes = !inQuotes;
      } else if (ch === delimiter && !inQuotes) {
        tokens.push(current.trim());
        current = '';
      } else {
        current += ch;
      }
    }
    tokens.push(current.trim());
    return tokens;
  }

  private normalizeText(value: string): string {
    return value
      .trim()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .replace(/\s+/g, ' ');
  }

  private parseCsvAmount(raw: string): number | null {
    const cleaned = raw.replace(/\u00A0/g, '').replace(/[R$\s"']/g, '').trim();
    if (!cleaned) return null;

    const lastComma = cleaned.lastIndexOf(',');
    const lastDot = cleaned.lastIndexOf('.');
    let normalized = cleaned;

    if (lastComma > lastDot) {
      normalized = cleaned.replace(/\./g, '').replace(',', '.');
    } else if (lastDot > lastComma && lastComma !== -1) {
      normalized = cleaned.replace(/,/g, '');
    } else if (lastDot !== -1 && /^\d{1,3}(\.\d{3})+$/.test(cleaned)) {
      normalized = cleaned.replace(/\./g, '');
    }

    const num = Number(normalized);
    return Number.isFinite(num) ? num : null;
  }

  uploadCsv(): void {
    if (!this.selectedCsvFile) return;
    this.errorMessage = '';
    this.successMessage = '';
    this.isImportingCsv = true;

    this.expenseService.importStatementFile(this.selectedCsvFile, this.csvCategoryId).subscribe({
      next: (result) => {
        this.isImportingCsv = false;
        const expensesCount = result.totalExpensesImported ?? result.expenses?.length ?? 0;
        const balancesCount = result.totalBalancesImported ?? result.balances?.length ?? 0;
        this.successMessage = `Importação concluída: ${expensesCount} despesa(s) e ${balancesCount} registro(s) de salário importado(s)!`;
        this.clearCsvSelection();
        this.loadExpenses();
        this.loadBalances();
      },
      error: (err) => {
        this.isImportingCsv = false;
        this.errorMessage = err.error?.message || 'Erro ao importar planilha.';
      }
    });
  }

  clearCsvSelection(): void {
    this.selectedCsvFile = null;
    this.csvPreviewItems = [];
    if (this.csvFileInput?.nativeElement) {
      this.csvFileInput.nativeElement.value = '';
    }
  }

  downloadSampleCsv(): void {
    const sample = [
      'Despesa;Janeiro;Fevereiro;Março;Abril;Maio;Junho;Julho;Agosto;Setembro;Outubro;Novembro;Dezembro',
      'Aluguel;1500,00;1500,00;1500,00;1500,00;1500,00;1500,00;1500,00;1500,00;1500,00;1500,00;1500,00;1500,00',
      'Energia;180,00;195,50;170,00;165,00;185,00;190,00;200,00;195,00;180,00;175,00;185,00;210,00',
      'Internet;120,00;120,00;120,00;120,00;120,00;120,00;120,00;120,00;120,00;120,00;120,00;120,00',
      'Supermercado;850,00;900,00;820,00;880,00;910,00;870,00;890,00;920,00;860,00;900,00;930,00;980,00',
      'Salário;4500,00;4500,00;4500,00;4500,00;4500,00;4500,00;4500,00;4500,00;4500,00;4500,00;4500,00;4500,00'
    ].join('\n');

    const blob = new Blob(['\uFEFF' + sample], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'modelo-financas-iana.csv';
    a.click();
    URL.revokeObjectURL(url);
  }

  // CRUD Despesa
  saveExpense(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.expenseForm.name.trim() || !this.expenseForm.amount || this.expenseForm.amount <= 0) {
      this.errorMessage = 'Preencha o nome e um valor válido maior que zero para a despesa.';
      return;
    }

    const payload: Partial<Expense> = {
      name: this.expenseForm.name.trim(),
      amount: Number(this.expenseForm.amount),
      description: this.expenseForm.description?.trim() || '',
      categoryId: this.expenseForm.categoryId || undefined
    };

    const obs = this.editingExpenseId
      ? this.expenseService.updateExpense(this.editingExpenseId, payload)
      : this.expenseService.createExpense(payload);

    obs.subscribe({
      next: () => {
        this.resetExpenseForm();
        this.loadExpenses();
      },
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao salvar despesa.')
    });
  }

  editExpense(expense: Expense): void {
    this.editingExpenseId = expense.id;
    this.expenseForm = {
      name: expense.name,
      amount: expense.amount,
      description: expense.description || '',
      categoryId: expense.categoryId
    };
  }

  requestDeleteExpense(expense: Expense): void {
    this.expenseToDelete = expense;
  }

  confirmDeleteExpense(): void {
    if (!this.expenseToDelete) return;
    const id = this.expenseToDelete.id;
    this.expenseToDelete = null;
    this.expenseService.deleteExpense(id).subscribe({
      next: () => this.loadExpenses(),
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao excluir despesa.')
    });
  }

  resetExpenseForm(): void {
    this.editingExpenseId = null;
    this.expenseForm = { name: '', amount: null, description: '', categoryId: undefined };
  }

  // CRUD Saldo
  saveBalance(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.balanceForm.source.trim() || !this.balanceForm.amount || this.balanceForm.amount <= 0) {
      this.errorMessage = 'Preencha a origem e um valor válido maior que zero para o saldo.';
      return;
    }

    const payload: Partial<Balance> = {
      source: this.balanceForm.source.trim(),
      amount: Number(this.balanceForm.amount),
      description: this.balanceForm.description?.trim() || undefined
    };

    const obs = this.editingBalanceId
      ? this.balanceService.updateBalance(this.editingBalanceId, payload)
      : this.balanceService.createBalance(payload);

    obs.subscribe({
      next: () => {
        this.resetBalanceForm();
        this.loadBalances();
      },
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao salvar saldo.')
    });
  }

  editBalance(balance: Balance): void {
    this.editingBalanceId = balance.id;
    this.balanceForm = {
      source: balance.source,
      amount: balance.amount,
      description: balance.description || ''
    };
  }

  requestDeleteBalance(balance: Balance): void {
    this.balanceToDelete = balance;
  }

  confirmDeleteBalance(): void {
    if (!this.balanceToDelete) return;
    const id = this.balanceToDelete.id;
    this.balanceToDelete = null;
    this.balanceService.deleteBalance(id).subscribe({
      next: () => this.loadBalances(),
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao excluir saldo.')
    });
  }

  resetBalanceForm(): void {
    this.editingBalanceId = null;
    this.balanceForm = { source: '', amount: null, description: '' };
  }
}
