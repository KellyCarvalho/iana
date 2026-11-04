import { Component, OnInit } from '@angular/core';
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
        <p style="color: #94a3b8; font-size: 0.9rem; margin-top: 0.2rem;">
          Gerencie suas despesas, importe faturas de cartão via CSV e acompanhe seu saldo.
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
      <div class="card" style="margin-bottom: 0; border-left: 4px solid #10b981;">
        <div style="font-size: 0.8rem; color: #94a3b8; text-transform: uppercase; font-weight: 600;">
          Total de Saldos / Entradas
        </div>
        <div style="font-size: 1.6rem; font-weight: 700; color: #34d399; margin-top: 0.35rem;">
          {{ formatCurrency(totalBalances) }}
        </div>
        <div style="font-size: 0.78rem; color: #64748b; margin-top: 0.25rem;">
          {{ balances.length }} registro(s) de saldo
        </div>
      </div>

      <div class="card" style="margin-bottom: 0; border-left: 4px solid #ef4444;">
        <div style="font-size: 0.8rem; color: #94a3b8; text-transform: uppercase; font-weight: 600;">
          Total de Despesas
        </div>
        <div style="font-size: 1.6rem; font-weight: 700; color: #f87171; margin-top: 0.35rem;">
          {{ formatCurrency(totalExpenses) }}
        </div>
        <div style="font-size: 0.78rem; color: #64748b; margin-top: 0.25rem;">
          {{ expenses.length }} despesa(s) lançada(s)
        </div>
      </div>

      <div
        class="card"
        style="margin-bottom: 0;"
        [style.border-left]="netBalance >= 0 ? '4px solid #6366f1' : '4px solid #f59e0b'">
        <div style="font-size: 0.8rem; color: #94a3b8; text-transform: uppercase; font-weight: 600;">
          Saldo Líquido (Saldos - Despesas)
        </div>
        <div
          style="font-size: 1.6rem; font-weight: 700; margin-top: 0.35rem;"
          [style.color]="netBalance >= 0 ? '#818cf8' : '#fbbf24'">
          {{ formatCurrency(netBalance) }}
        </div>
        <div style="font-size: 0.78rem; color: #64748b; margin-top: 0.25rem;">
          {{ balances.length === 0 ? 'Cadastre um saldo (opcional) para abater' : (netBalance >= 0 ? 'Dentro do orçamento' : 'Despesas superam o saldo informado') }}
        </div>
      </div>
    </div>

    <!-- SEÇÃO DE DESPESAS -->
    <ng-container *ngIf="activeTab === 'all' || activeTab === 'expenses'">
      <!-- Card de Importação de Fatura de Cartão ou Boleto (CSV / PDF) -->
      <div class="card" style="border: 1px dashed #4f46e5; background: rgba(30, 41, 59, 0.85);">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 0.85rem;">
          <div>
            <h2 style="font-size: 1.1rem; color: #f8fafc; display: flex; align-items: center; gap: 0.5rem;">
              💳📄 Importar Fatura de Cartão ou Boleto (CSV ou PDF)
            </h2>
            <p style="font-size: 0.84rem; color: #94a3b8; margin-top: 0.25rem;">
              <strong>Não precisa de template!</strong> Identificamos automaticamente as colunas e lançamentos de qualquer banco (Nubank, Inter, Itaú, Bradesco, C6, Santander) em <strong>CSV</strong> ou <strong>PDF</strong>.
            </p>
          </div>
          <button type="button" class="btn btn-secondary btn-sm" (click)="downloadSampleCsv()">
            📥 Baixar Exemplo CSV
          </button>
        </div>

        <div class="form-grid" style="align-items: end;">
          <div class="form-group">
            <label>Arquivo da Fatura ou Boleto (.CSV ou .PDF) *</label>
            <input
              type="file"
              accept=".csv,.pdf,text/csv,application/pdf"
              (change)="onCsvFileSelected($event)"
              style="padding: 0.5rem;" />
          </div>
          <div class="form-group">
            <label>Categoria para a Fatura (opcional)</label>
            <select [(ngModel)]="csvCategoryId" name="csvCategoryId">
              <option [ngValue]="undefined">Automático (usar do arquivo ou sem categoria)</option>
              <option *ngFor="let c of categories" [value]="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="actions" style="margin-bottom: 0.15rem;">
            <button
              type="button"
              class="btn btn-primary"
              [disabled]="!selectedCsvFile || isImportingCsv"
              (click)="uploadCsv()">
              {{ isImportingCsv ? 'Processando arquivo...' : 'Importar Despesas' }}
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

        <!-- Indicador quando PDF é selecionado -->
        <div *ngIf="isPdfSelected && selectedCsvFile" style="margin-top: 0.85rem; padding: 0.75rem 1rem; border-radius: 8px; background: rgba(99, 102, 241, 0.12); border: 1px solid rgba(99, 102, 241, 0.35); color: #cbd5e1; font-size: 0.86rem;">
          📄 <strong>PDF selecionado:</strong> {{ selectedCsvFile.name }} — Clique em <strong>"Importar Despesas"</strong> para extrair automaticamente as compras da fatura ou dados do boleto.
        </div>

        <!-- Preview rápido do CSV selecionado -->
        <div *ngIf="csvPreviewItems.length > 0" style="margin-top: 1rem; padding-top: 0.85rem; border-top: 1px solid #334155;">
          <div style="font-size: 0.82rem; color: #cbd5e1; margin-bottom: 0.5rem; font-weight: 600;">
            Pré-visualização do arquivo ({{ csvPreviewItems.length }} lançamento(s) identificado(s)):
          </div>
          <div style="max-height: 180px; overflow-y: auto; border: 1px solid #334155; border-radius: 8px;">
            <table>
              <thead>
                <tr>
                  <th>Estabelecimento / Nome</th>
                  <th>Detalhes / Data</th>
                  <th>Valor</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of csvPreviewItems">
                  <td style="padding: 0.5rem 0.85rem; font-weight: 500;">{{ item.name }}</td>
                  <td style="padding: 0.5rem 0.85rem; color: #94a3b8; font-size: 0.82rem;">{{ item.description }}</td>
                  <td style="padding: 0.5rem 0.85rem; color: #f87171; font-weight: 600;">{{ formatCurrency(item.amount) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Formulário manual de Despesa -->
      <div class="card">
        <h2 style="font-size: 1.15rem; margin-bottom: 1rem; color: #f8fafc;">
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
          <h2 style="font-size: 1.1rem;">Despesas Cadastradas</h2>
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
              <td style="color: #cbd5e1;">{{ e.description || '-' }}</td>
              <td>
                <span class="badge badge-user">{{ getCategoryName(e.categoryId) }}</span>
              </td>
              <td style="font-weight: 700; color: #f87171;">
                - {{ formatCurrency(e.amount) }}
              </td>
              <td class="actions">
                <button class="btn btn-secondary btn-sm" (click)="editExpense(e)">Editar</button>
                <button class="btn btn-danger btn-sm" (click)="requestDeleteExpense(e)">Excluir</button>
              </td>
            </tr>
            <tr *ngIf="expenses.length === 0">
              <td colspan="5" style="text-align: center; color: #94a3b8;">Nenhuma despesa cadastrada.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </ng-container>

    <!-- SEÇÃO DE SALDOS (OPCIONAL) -->
    <ng-container *ngIf="activeTab === 'all' || activeTab === 'balances'">
      <div class="card">
        <h2 style="font-size: 1.15rem; margin-bottom: 1rem; color: #f8fafc;">
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
        <h2 style="font-size: 1.1rem; margin-bottom: 1rem;">Saldos / Entradas Cadastradas</h2>
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
              <td style="color: #cbd5e1;">{{ b.description || '-' }}</td>
              <td style="font-weight: 700; color: #34d399;">
                + {{ formatCurrency(b.amount) }}
              </td>
              <td class="actions">
                <button class="btn btn-secondary btn-sm" (click)="editBalance(b)">Editar</button>
                <button class="btn btn-danger btn-sm" (click)="requestDeleteBalance(b)">Excluir</button>
              </td>
            </tr>
            <tr *ngIf="balances.length === 0">
              <td colspan="4" style="text-align: center; color: #94a3b8;">Nenhum saldo cadastrado ainda.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </ng-container>

    <!-- Modal de confirmação de exclusão de Despesa -->
    <div
      *ngIf="expenseToDelete"
      style="position: fixed; inset: 0; background: rgba(15, 23, 42, 0.75); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;">
      <div class="card" style="max-width: 440px; width: 100%; margin: 0; border: 1px solid #475569;">
        <h3 style="margin-bottom: 0.75rem; font-size: 1.15rem; color: #f8fafc;">Confirmar Exclusão de Despesa</h3>
        <p style="color: #cbd5e1; margin-bottom: 1.25rem; line-height: 1.5;">
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
      style="position: fixed; inset: 0; background: rgba(15, 23, 42, 0.75); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;">
      <div class="card" style="max-width: 440px; width: 100%; margin: 0; border: 1px solid #475569;">
        <h3 style="margin-bottom: 0.75rem; font-size: 1.15rem; color: #f8fafc;">Confirmar Exclusão de Saldo</h3>
        <p style="color: #cbd5e1; margin-bottom: 1.25rem; line-height: 1.5;">
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

  selectedCsvFile: File | null = null;
  isPdfSelected = false;
  csvCategoryId?: string = undefined;
  isImportingCsv = false;
  csvPreviewItems: Array<{ name: string; amount: number; description: string }> = [];

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

  // Importação de CSV ou PDF de Fatura de Cartão / Boleto
  onCsvFileSelected(event: Event): void {
    this.errorMessage = '';
    this.successMessage = '';
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      this.clearCsvSelection();
      return;
    }
    this.selectedCsvFile = input.files[0];
    const nameLower = this.selectedCsvFile.name.toLowerCase();
    this.isPdfSelected = nameLower.endsWith('.pdf') || this.selectedCsvFile.type === 'application/pdf';
    if (this.isPdfSelected) {
      this.csvPreviewItems = [];
    } else {
      this.buildClientSideCsvPreview(this.selectedCsvFile);
    }
  }

  private buildClientSideCsvPreview(file: File): void {
    const reader = new FileReader();
    reader.onload = () => {
      const text = (reader.result as string) || '';
      const lines = text
        .replace(/\uFEFF/g, '')
        .split(/\r?\n/)
        .map((l) => l.trim())
        .filter((l) => l.length > 0);

      if (lines.length === 0) {
        this.csvPreviewItems = [];
        return;
      }

      const delimiter = (lines[0].match(/;/g) || []).length > (lines[0].match(/,/g) || []).length ? ';' : ',';
      const firstCols = lines[0].split(delimiter).map((c) => c.replace(/^"|"$/g, '').trim().toLowerCase());

      let nameIdx = firstCols.findIndex((c) =>
        ['title', 'titulo', 'título', 'nome', 'name', 'estabelecimento', 'lancamento', 'lançamento', 'historico', 'histórico'].includes(c)
      );
      let amountIdx = firstCols.findIndex((c) => ['amount', 'valor', 'value', 'preco', 'preço', 'total'].includes(c));
      let descIdx = firstCols.findIndex((c) => ['description', 'descricao', 'descrição', 'detalhes', 'obs'].includes(c));
      let dateIdx = firstCols.findIndex((c) => ['date', 'data', 'vencimento'].includes(c));
      let catIdx = firstCols.findIndex((c) => ['category', 'categoria', 'tipo'].includes(c));

      const hasHeader = nameIdx !== -1 || amountIdx !== -1;
      if (!hasHeader) {
        nameIdx = 0;
        amountIdx = firstCols.length > 1 ? 1 : 0;
        descIdx = firstCols.length > 2 ? 2 : -1;
      }

      const preview: Array<{ name: string; amount: number; description: string }> = [];
      for (let i = hasHeader ? 1 : 0; i < lines.length; i++) {
        const cols = lines[i].split(delimiter).map((c) => c.replace(/^"|"$/g, '').trim());
        const rawName = cols[nameIdx] || '';
        const rawAmount = (cols[amountIdx] || '').replace(/[R$\s]/g, '');
        const normalizedAmount =
          rawAmount.lastIndexOf(',') > rawAmount.lastIndexOf('.')
            ? rawAmount.replace(/\./g, '').replace(',', '.')
            : rawAmount.replace(/,/g, '');
        const amount = Number(normalizedAmount);

        if (!rawName || isNaN(amount) || amount <= 0 || rawName.toLowerCase().includes('pagamento recebido')) {
          continue;
        }

        const parts: string[] = [];
        if (descIdx !== -1 && cols[descIdx]) parts.push(cols[descIdx]);
        if (dateIdx !== -1 && cols[dateIdx]) parts.push(`Data: ${cols[dateIdx]}`);
        if (catIdx !== -1 && cols[catIdx]) parts.push(`Cat: ${cols[catIdx]}`);

        preview.push({
          name: rawName,
          amount,
          description: parts.join(' | ') || 'Importado via fatura CSV'
        });
      }
      this.csvPreviewItems = preview;
    };
    reader.readAsText(file, 'UTF-8');
  }

  uploadCsv(): void {
    if (!this.selectedCsvFile) return;
    this.errorMessage = '';
    this.successMessage = '';
    this.isImportingCsv = true;

    this.expenseService.importStatementFile(this.selectedCsvFile, this.csvCategoryId).subscribe({
      next: (imported) => {
        this.isImportingCsv = false;
        this.successMessage = `${imported.length} despesa(s) importada(s) com sucesso!`;
        this.clearCsvSelection();
        this.loadExpenses();
      },
      error: (err) => {
        this.isImportingCsv = false;
        this.errorMessage = err.error?.message || 'Erro ao importar arquivo.';
      }
    });
  }

  clearCsvSelection(): void {
    this.selectedCsvFile = null;
    this.isPdfSelected = false;
    this.csvPreviewItems = [];
  }

  downloadSampleCsv(): void {
    const sample = [
      'date,category,title,amount',
      '2026-11-01,restaurante,iFood *Restaurante Sabor,64.90',
      '2026-11-02,transporte,Uber *Trip,28.50',
      '2026-11-03,supermercado,Supermercado Extra,342.15',
      '2026-11-03,servicos,Netflix.Com,55.90'
    ].join('\n');

    const blob = new Blob([sample], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'fatura-cartao-exemplo.csv';
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
