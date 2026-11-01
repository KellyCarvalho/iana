import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { Book, Category, Link } from '../../core/models/api.models';

@Component({
  selector: 'app-links',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <h1>Gerenciamento de Links</h1>
    </div>

    <div *ngIf="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div class="card">
      <h2 style="font-size: 1.1rem; margin-bottom: 1rem;">
        {{ editingId ? 'Editar Link' : 'Novo Link' }}
      </h2>
      <form (ngSubmit)="saveLink()">
        <div class="form-grid">
          <div class="form-group">
            <label>URL *</label>
            <input type="url" [(ngModel)]="form.url" name="url" placeholder="https://..." required />
          </div>
          <div class="form-group">
            <label>Título</label>
            <input type="text" [(ngModel)]="form.title" name="title" />
          </div>
          <div class="form-group">
            <label>Livro Associado</label>
            <select [(ngModel)]="form.bookId" name="bookId">
              <option [ngValue]="undefined">Nenhum</option>
              <option *ngFor="let b of books" [value]="b.id">{{ b.title }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Categoria Associada</label>
            <select [(ngModel)]="form.categoryId" name="categoryId">
              <option [ngValue]="undefined">Nenhuma</option>
              <option *ngFor="let c of categories" [value]="c.id">{{ c.name }}</option>
            </select>
          </div>
        </div>
        <div class="form-group" style="margin-bottom: 1rem;">
          <label>Descrição</label>
          <input type="text" [(ngModel)]="form.description" name="description" />
        </div>
        <div class="actions">
          <button type="submit" class="btn btn-primary">{{ editingId ? 'Salvar Alterações' : 'Cadastrar e Validar Link' }}</button>
          <button *ngIf="editingId" type="button" class="btn btn-secondary" (click)="resetForm()">Cancelar</button>
        </div>
      </form>
    </div>

    <div class="card">
      <table>
        <thead>
          <tr>
            <th>Título</th>
            <th>Link (URL)</th>
            <th>Livro</th>
            <th>Categoria</th>
            <th>Status HTTP</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let l of links">
            <td style="font-weight: 600;">{{ l.title || 'Sem título' }}</td>
            <td>
              <a [href]="formatExternalUrl(l.url)" target="_blank" rel="noopener noreferrer" style="color: #818cf8; font-weight: 500;">
                {{ l.url }} ↗
              </a>
            </td>
            <td>{{ getBookTitle(l.bookId) }}</td>
            <td>{{ getCategoryName(l.categoryId) }}</td>
            <td>
              <span class="badge" [ngClass]="l.active ? 'badge-success' : 'badge-danger'">
                {{ l.active ? 'Acessível' : 'Inacessível' }}
              </span>
            </td>
            <td class="actions">
              <a [href]="formatExternalUrl(l.url)" target="_blank" rel="noopener noreferrer" class="btn btn-primary btn-sm" style="text-decoration: none;">Ir para o Link ↗</a>
              <button class="btn btn-secondary btn-sm" (click)="editLink(l)">Editar</button>
              <button class="btn btn-danger btn-sm" (click)="deleteLink(l.id)">Excluir</button>
            </td>
          </tr>
          <tr *ngIf="links.length === 0">
            <td colspan="6" style="text-align: center; color: #94a3b8;">Nenhum link cadastrado.</td>
          </tr>
        </tbody>
      </table>
    </div>
  `
})
export class LinksComponent implements OnInit {
  links: Link[] = [];
  books: Book[] = [];
  categories: Category[] = [];
  editingId: string | null = null;
  errorMessage = '';
  form: Partial<Link> = { url: '', title: '', description: '', bookId: undefined, categoryId: undefined };

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.api.getLinks().subscribe({ next: (res) => (this.links = res) });
    this.api.getBooks().subscribe({ next: (res) => (this.books = res) });
    this.api.getCategories().subscribe({ next: (res) => (this.categories = res) });
  }

  formatExternalUrl(url?: string): string {
    if (!url) return '#';
    const trimmed = url.trim();
    return /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
  }

  getBookTitle(bookId?: string): string {
    if (!bookId) return '-';
    return this.books.find((b) => b.id === bookId)?.title || bookId;
  }

  getCategoryName(categoryId?: string): string {
    if (!categoryId) return '-';
    return this.categories.find((c) => c.id === categoryId)?.name || categoryId;
  }

  saveLink(): void {
    this.errorMessage = '';
    const obs = this.editingId
      ? this.api.updateLink(this.editingId, this.form)
      : this.api.createLink(this.form);

    obs.subscribe({
      next: () => {
        this.resetForm();
        this.loadData();
      },
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao salvar link.')
    });
  }

  editLink(link: Link): void {
    this.editingId = link.id;
    this.form = {
      url: link.url,
      title: link.title,
      description: link.description,
      bookId: link.bookId,
      categoryId: link.categoryId
    };
  }

  deleteLink(id: string): void {
    this.api.deleteLink(id).subscribe({
      next: () => this.loadData(),
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao excluir link.')
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { url: '', title: '', description: '', bookId: undefined, categoryId: undefined };
  }
}
