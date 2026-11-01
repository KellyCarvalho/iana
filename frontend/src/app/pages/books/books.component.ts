import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ApiService } from '../../core/services/api.service';
import { Book, Category } from '../../core/models/api.models';

@Component({
  selector: 'app-books',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <h1>Livros</h1>
    </div>

    <div *ngIf="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div class="card">
      <h2 style="font-size: 1.1rem; margin-bottom: 1rem;">
        {{ editingId ? 'Editar Livro' : 'Novo Livro' }}
      </h2>
      <form (ngSubmit)="saveBook()">
        <div class="form-grid">
          <div class="form-group">
            <label>Título *</label>
            <input type="text" [(ngModel)]="form.title" name="title" placeholder="Ex: Clean Code" required />
          </div>
          <div class="form-group">
            <label>Autor *</label>
            <input type="text" [(ngModel)]="form.author" name="author" placeholder="Ex: Robert C. Martin" required />
          </div>
          <div class="form-group">
            <label>Categoria</label>
            <select [(ngModel)]="form.categoryId" name="categoryId">
              <option [ngValue]="undefined">Sem categoria</option>
              <option *ngFor="let c of categories" [value]="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>Link do PDF / Livro *</label>
            <input type="text" [(ngModel)]="form.linkUrl" name="linkUrl" placeholder="https://exemplo.com/livro.pdf" required />
          </div>
        </div>
        <div class="form-group" style="margin-bottom: 1rem;">
          <label>Descrição</label>
          <input type="text" [(ngModel)]="form.description" name="description" placeholder="Resumo ou observações sobre o livro" />
        </div>
        <div class="actions">
          <button type="submit" class="btn btn-primary">{{ editingId ? 'Salvar Alterações' : 'Cadastrar Livro' }}</button>
          <button *ngIf="editingId" type="button" class="btn btn-secondary" (click)="resetForm()">Cancelar</button>
        </div>
      </form>
    </div>

    <div class="card">
      <table>
        <thead>
          <tr>
            <th style="width: 300px;">Preview do PDF</th>
            <th>Autor</th>
            <th>Descrição</th>
            <th>Categoria</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let b of books">
            <td>
              <div style="display: flex; flex-direction: column; gap: 0.5rem; max-width: 280px;">
                <div style="font-weight: 700; color: #f8fafc; font-size: 0.95rem;">
                  {{ b.title }}
                </div>

                <ng-container *ngIf="getPrimaryBookUrl(b) as pdfUrl; else noPdfTemplate">
                  <div style="position: relative; width: 260px; min-height: 150px; border-radius: 0.5rem; overflow: hidden; border: 1px solid #334155; background: linear-gradient(145deg, #0f172a 0%, #1e293b 100%); display: flex; flex-direction: column; justify-content: space-between; padding: 0.85rem;">
                    <ng-container *ngIf="activePreviewBookId === b.id; else staticPreviewCard">
                      <iframe
                        [src]="getSafePreviewUrl(pdfUrl)"
                        sandbox="allow-scripts allow-same-origin"
                        referrerpolicy="no-referrer"
                        style="width: 100%; height: 160px; border: none; border-radius: 0.35rem; background: #1e293b;"
                        title="Preview do PDF">
                      </iframe>
                      <button
                        type="button"
                        class="btn btn-secondary btn-sm"
                        style="margin-top: 0.4rem; width: 100%;"
                        (click)="togglePreview(b.id)">
                        Fechar Preview
                      </button>
                    </ng-container>

                    <ng-template #staticPreviewCard>
                      <div style="display: flex; align-items: flex-start; gap: 0.75rem;">
                        <div style="width: 42px; height: 52px; border-radius: 0.35rem; background: rgba(239, 68, 68, 0.15); border: 1px solid rgba(239, 68, 68, 0.4); display: flex; flex-direction: column; align-items: center; justify-content: center; flex-shrink: 0;">
                          <span style="font-size: 1.1rem;">📕</span>
                          <span style="font-size: 0.6rem; font-weight: 700; color: #f87171;">PDF</span>
                        </div>
                        <div style="overflow: hidden;">
                          <div style="font-weight: 600; color: #f8fafc; font-size: 0.88rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                            {{ b.title }}
                          </div>
                          <div style="color: #94a3b8; font-size: 0.75rem; margin-top: 0.15rem;">
                            {{ b.author }}
                          </div>
                          <div style="color: #64748b; font-size: 0.7rem; margin-top: 0.25rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                            {{ pdfUrl }}
                          </div>
                        </div>
                      </div>

                      <div style="display: flex; gap: 0.4rem; margin-top: 0.75rem; flex-wrap: wrap;">
                        <a
                          [href]="formatExternalUrl(pdfUrl)"
                          target="_blank"
                          rel="noopener noreferrer"
                          class="btn btn-primary btn-sm"
                          style="text-decoration: none; flex: 1; text-align: center;">
                          📄 Abrir PDF / Link ↗
                        </a>
                        <button
                          type="button"
                          class="btn btn-secondary btn-sm"
                          (click)="togglePreview(b.id)"
                          title="Carregar visualização embutida">
                          👁️ Preview
                        </button>
                      </div>
                    </ng-template>
                  </div>

                  <div *ngIf="b.links && b.links.length > 1" style="display: flex; flex-wrap: wrap; gap: 0.35rem;">
                    <ng-container *ngFor="let l of b.links; let i = index">
                      <a
                        *ngIf="l.url && l.url !== pdfUrl"
                        [href]="formatExternalUrl(l.url)"
                        target="_blank"
                        rel="noopener noreferrer"
                        class="btn btn-secondary btn-sm"
                        style="text-decoration: none;">
                        🔗 Link {{ i + 1 }} ↗
                      </a>
                    </ng-container>
                  </div>
                </ng-container>

                <ng-template #noPdfTemplate>
                  <div style="width: 260px; height: 110px; border-radius: 0.5rem; border: 1px dashed #334155; display: flex; align-items: center; justify-content: center; color: #64748b; font-size: 0.85rem;">
                    Sem PDF / Link cadastrado
                  </div>
                </ng-template>
              </div>
            </td>
            <td style="font-weight: 500; vertical-align: middle;">{{ b.author }}</td>
            <td style="color: #cbd5e1; vertical-align: middle;">{{ b.description || '-' }}</td>
            <td style="vertical-align: middle;">
              <span class="badge badge-user">{{ getCategoryName(b.categoryId) }}</span>
            </td>
            <td class="actions" style="vertical-align: middle;">
              <button class="btn btn-secondary btn-sm" (click)="editBook(b)">Editar</button>
              <button class="btn btn-danger btn-sm" (click)="requestDeleteBook(b)">Excluir</button>
            </td>
          </tr>
          <tr *ngIf="books.length === 0">
            <td colspan="5" style="text-align: center; color: #94a3b8;">Nenhum livro cadastrado.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      *ngIf="bookToDelete"
      style="position: fixed; inset: 0; background: rgba(15, 23, 42, 0.75); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;">
      <div class="card" style="max-width: 440px; width: 100%; margin: 0; border: 1px solid #475569;">
        <h3 style="margin-bottom: 0.75rem; font-size: 1.15rem; color: #f8fafc;">Confirmar Exclusão</h3>
        <p style="color: #cbd5e1; margin-bottom: 1.25rem; line-height: 1.5;">
          Tem certeza que deseja excluir o livro <strong>"{{ bookToDelete.title }}"</strong>? Esta ação não pode ser desfeita.
        </p>
        <div class="actions" style="justify-content: flex-end;">
          <button type="button" class="btn btn-secondary" (click)="cancelDeleteBook()">Cancelar</button>
          <button type="button" class="btn btn-danger" (click)="confirmDeleteBook()">Confirmar Exclusão</button>
        </div>
      </div>
    </div>
  `
})
export class BooksComponent implements OnInit {
  books: Book[] = [];
  categories: Category[] = [];
  editingId: string | null = null;
  bookToDelete: Book | null = null;
  activePreviewBookId: string | null = null;
  errorMessage = '';
  private previewCache = new Map<string, SafeResourceUrl>();

  form: {
    title: string;
    author: string;
    description: string;
    linkUrl: string;
    categoryId?: string;
  } = { title: '', author: '', description: '', linkUrl: '', categoryId: undefined };

  constructor(private api: ApiService, private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.errorMessage = '';
    this.api.getBooks().subscribe({
      next: (res) => (this.books = res || []),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Não foi possível carregar a listagem de livros. Faça login novamente.';
      }
    });
    this.api.getCategories().subscribe({
      next: (res) => (this.categories = res || [])
    });
  }

  togglePreview(bookId: string): void {
    this.activePreviewBookId = this.activePreviewBookId === bookId ? null : bookId;
  }

  getPrimaryBookUrl(book: Book): string | null {
    if (book.linkUrl && book.linkUrl.trim()) {
      return book.linkUrl.trim();
    }
    if (book.coverUrl && book.coverUrl.trim()) {
      return book.coverUrl.trim();
    }
    if (book.links && book.links.length > 0 && book.links[0].url) {
      return book.links[0].url.trim();
    }
    return null;
  }

  formatExternalUrl(url?: string | null): string {
    if (!url) return '#';
    const trimmed = url.trim();
    if (/^(javascript|data|vbscript|file):/i.test(trimmed)) {
      return '#';
    }
    return /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
  }

  getSafePreviewUrl(rawUrl: string): SafeResourceUrl {
    const formatted = this.formatExternalUrl(rawUrl);
    if (this.previewCache.has(formatted)) {
      return this.previewCache.get(formatted)!;
    }

    let embedUrl = formatted;
    // Converte links do Google Drive (/file/d/ID/view) para /preview automaticamente
    const driveMatch = formatted.match(/drive\.google\.com\/file\/d\/([^/]+)/i);
    if (driveMatch && driveMatch[1]) {
      embedUrl = `https://drive.google.com/file/d/${driveMatch[1]}/preview`;
    } else if (/\.pdf($|\?)/i.test(formatted) && !formatted.includes('#')) {
      embedUrl = `${formatted}#toolbar=0&navpanes=0&scrollbar=0`;
    }

    const safe = this.sanitizer.bypassSecurityTrustResourceUrl(embedUrl);
    this.previewCache.set(formatted, safe);
    return safe;
  }

  getCategoryName(categoryId?: string): string {
    if (!categoryId) return 'Sem categoria';
    return this.categories.find((c) => c.id === categoryId)?.name || categoryId;
  }

  saveBook(): void {
    this.errorMessage = '';
    const payload: Partial<Book> = {
      title: this.form.title,
      author: this.form.author,
      description: this.form.description,
      coverUrl: this.form.linkUrl,
      linkUrl: this.form.linkUrl,
      categoryId: this.form.categoryId
    };

    const obs = this.editingId
      ? this.api.updateBook(this.editingId, payload)
      : this.api.createBook(payload);

    obs.subscribe({
      next: () => {
        this.resetForm();
        this.loadData();
      },
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao salvar livro.')
    });
  }

  editBook(book: Book): void {
    this.editingId = book.id;
    this.form = {
      title: book.title,
      author: book.author,
      description: book.description || '',
      linkUrl: this.getPrimaryBookUrl(book) || '',
      categoryId: book.categoryId
    };
  }

  requestDeleteBook(book: Book): void {
    this.bookToDelete = book;
  }

  cancelDeleteBook(): void {
    this.bookToDelete = null;
  }

  confirmDeleteBook(): void {
    if (!this.bookToDelete) return;
    const id = this.bookToDelete.id;
    this.bookToDelete = null;
    this.api.deleteBook(id).subscribe({
      next: () => this.loadData(),
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao excluir livro.')
    });
  }

  resetForm(): void {
    this.editingId = null;
    this.form = { title: '', author: '', description: '', linkUrl: '', categoryId: undefined };
  }
}
