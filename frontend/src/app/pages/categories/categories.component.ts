import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { Category } from '../../core/models/api.models';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <h1>Gerenciamento de Categorias</h1>
    </div>

    <div *ngIf="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div class="card">
      <h2 style="font-size: 1.1rem; margin-bottom: 1rem;">
        {{ editingId ? 'Editar Categoria' : 'Nova Categoria' }}
      </h2>
      <form (ngSubmit)="saveCategory()">
        <div class="form-grid">
          <div class="form-group">
            <label>Nome *</label>
            <input type="text" [(ngModel)]="form.name" name="name" required />
          </div>
          <div class="form-group">
            <label>Descrição</label>
            <input type="text" [(ngModel)]="form.description" name="description" />
          </div>
        </div>
        <div class="actions">
          <button type="submit" class="btn btn-primary">{{ editingId ? 'Salvar Alterações' : 'Cadastrar Categoria' }}</button>
          <button *ngIf="editingId" type="button" class="btn btn-secondary" (click)="resetForm()">Cancelar</button>
        </div>
      </form>
    </div>

    <div class="card">
      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Descrição</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let c of categories">
            <td style="font-weight: 600;">{{ c.name }}</td>
            <td style="color: #475569;">{{ c.description || '-' }}</td>
            <td class="actions">
              <button class="btn btn-secondary btn-sm" (click)="editCategory(c)">Editar</button>
              <button class="btn btn-danger btn-sm" (click)="requestDeleteCategory(c)">Excluir</button>
            </td>
          </tr>
          <tr *ngIf="categories.length === 0">
            <td colspan="3" style="text-align: center; color: #64748b;">Nenhuma categoria cadastrada.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      *ngIf="categoryToDelete"
      style="position: fixed; inset: 0; background: rgba(15, 23, 42, 0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;">
      <div class="card" style="max-width: 440px; width: 100%; margin: 0; border: 1px solid #bfdbfe;">
        <h3 style="margin-bottom: 0.75rem; font-size: 1.15rem; color: #1e3a8a;">Confirmar Exclusão</h3>
        <p style="color: #334155; margin-bottom: 1.25rem; line-height: 1.5;">
          Tem certeza que deseja excluir a categoria <strong>"{{ categoryToDelete.name }}"</strong>? Esta ação não pode ser desfeita.
        </p>
        <div class="actions" style="justify-content: flex-end;">
          <button type="button" class="btn btn-secondary" (click)="cancelDeleteCategory()">Cancelar</button>
          <button type="button" class="btn btn-danger" (click)="confirmDeleteCategory()">Confirmar Exclusão</button>
        </div>
      </div>
    </div>
  `
})
export class CategoriesComponent implements OnInit {
  categories: Category[] = [];
  editingId: string | null = null;
  categoryToDelete: Category | null = null;
  errorMessage = '';
  form: Partial<Category> = { name: '', description: '' };

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.api.getCategories().subscribe({ next: (res) => (this.categories = res) });
  }

  formatExternalUrl(url?: string): string {
    if (!url) return '#';
    const trimmed = url.trim();
    return /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
  }

  saveCategory(): void {
    this.errorMessage = '';
    const obs = this.editingId
      ? this.api.updateCategory(this.editingId, this.form)
      : this.api.createCategory(this.form);

    obs.subscribe({
      next: () => {
        this.resetForm();
        this.loadCategories();
      },
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao salvar categoria.')
    });
  }

  editCategory(category: Category): void {
    this.editingId = category.id;
    this.form = { name: category.name, description: category.description };
  }

  requestDeleteCategory(category: Category): void {
    this.categoryToDelete = category;
  }

  cancelDeleteCategory(): void {
    this.categoryToDelete = null;
  }

  confirmDeleteCategory(): void {
    if (!this.categoryToDelete) return;
    const id = this.categoryToDelete.id;
    this.categoryToDelete = null;
    this.api.deleteCategory(id).subscribe({
      next: () => this.loadCategories(),
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao excluir categoria.')
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { name: '', description: '' };
  }
}
