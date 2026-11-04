import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/api.models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-header">
      <h1>Gerenciamento de Usuários</h1>
    </div>

    <div *ngIf="errorMessage" class="alert alert-error">{{ errorMessage }}</div>
    <div *ngIf="successMessage" class="alert alert-success">{{ successMessage }}</div>

    <div class="card">
      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>E-mail</th>
            <th>Role</th>
            <th>E-mail Verificado / Link</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let u of users">
            <td style="font-weight: 600;">{{ u.name }}</td>
            <td>{{ u.email }}</td>
            <td>
              <span class="badge" [ngClass]="u.role === 'ROLE_ADMIN' ? 'badge-admin' : 'badge-user'">
                {{ u.role }}
              </span>
            </td>
            <td>
              <span class="badge" [ngClass]="u.emailVerified ? 'badge-success' : 'badge-danger'">
                {{ u.emailVerified ? 'Verificado' : 'Pendente' }}
              </span>
              <div *ngIf="!u.emailVerified && u.verificationLink" style="margin-top: 0.35rem;">
                <a [href]="u.verificationLink" target="_blank" rel="noopener noreferrer" style="color: #2563eb; font-size: 0.82rem; font-weight: 600;">
                  Confirmar E-mail (Link) ↗
                </a>
              </div>
            </td>
            <td>
              <span class="badge" [ngClass]="u.active ? 'badge-success' : 'badge-danger'">
                {{ u.active ? 'Ativo' : 'Inativo' }}
              </span>
            </td>
            <td class="actions">
              <button
                *ngIf="authService.isAdmin()"
                class="btn btn-secondary btn-sm"
                (click)="toggleRole(u)"
              >
                Tornar {{ u.role === 'ROLE_ADMIN' ? 'USER' : 'ADMIN' }}
              </button>
              <button
                *ngIf="authService.isAdmin()"
                class="btn btn-danger btn-sm"
                (click)="requestDeleteUser(u)"
              >
                Excluir
              </button>
            </td>
          </tr>
          <tr *ngIf="users.length === 0">
            <td colspan="6" style="text-align: center; color: #64748b;">Nenhum usuário encontrado.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div
      *ngIf="userToDelete"
      style="position: fixed; inset: 0; background: rgba(15, 23, 42, 0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; padding: 1rem;">
      <div class="card" style="max-width: 440px; width: 100%; margin: 0; border: 1px solid #bfdbfe;">
        <h3 style="margin-bottom: 0.75rem; font-size: 1.15rem; color: #1e3a8a;">Confirmar Exclusão</h3>
        <p style="color: #334155; margin-bottom: 1.25rem; line-height: 1.5;">
          Tem certeza que deseja excluir o usuário <strong>"{{ userToDelete.name }}"</strong> ({{ userToDelete.email }})? Esta ação não pode ser desfeita.
        </p>
        <div class="actions" style="justify-content: flex-end;">
          <button type="button" class="btn btn-secondary" (click)="cancelDeleteUser()">Cancelar</button>
          <button type="button" class="btn btn-danger" (click)="confirmDeleteUser()">Confirmar Exclusão</button>
        </div>
      </div>
    </div>
  `
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  userToDelete: User | null = null;
  errorMessage = '';
  successMessage = '';

  constructor(private api: ApiService, public authService: AuthService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.api.getUsers().subscribe({ next: (res) => (this.users = res) });
  }

  toggleRole(user: User): void {
    this.errorMessage = '';
    this.successMessage = '';
    const newRole = user.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
    this.api.updateUserRole(user.id, newRole).subscribe({
      next: () => {
        this.successMessage = `Role de ${user.name} alterada para ${newRole}!`;
        this.loadUsers();
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Apenas ROLE_ADMIN pode alterar a role de outro usuário.';
      }
    });
  }

  requestDeleteUser(user: User): void {
    this.userToDelete = user;
  }

  cancelDeleteUser(): void {
    this.userToDelete = null;
  }

  confirmDeleteUser(): void {
    if (!this.userToDelete) return;
    const id = this.userToDelete.id;
    this.userToDelete = null;
    this.api.deleteUser(id).subscribe({
      next: () => this.loadUsers(),
      error: (err) => (this.errorMessage = err.error?.message || 'Erro ao excluir usuário.')
    });
  }
}
