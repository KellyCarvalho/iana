import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav *ngIf="authService.isLoggedIn()" style="background: #1e293b; border-bottom: 1px solid #334155; padding: 0.9rem 1.5rem;">
      <div style="max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <div style="display: flex; align-items: center; gap: 2rem;">
          <span style="font-weight: 700; font-size: 1.25rem; color: #818cf8; letter-spacing: -0.02em;">IANA</span>
          <div style="display: flex; gap: 1.25rem;">
            <a routerLink="/books" routerLinkActive="active-link" style="color: #cbd5e1; text-decoration: none; font-weight: 500;">Livros</a>
            <a routerLink="/categories" routerLinkActive="active-link" style="color: #cbd5e1; text-decoration: none; font-weight: 500;">Categorias</a>
            <a routerLink="/users" routerLinkActive="active-link" style="color: #cbd5e1; text-decoration: none; font-weight: 500;">Usuários</a>
          </div>
        </div>
        <div style="display: flex; align-items: center; gap: 0.85rem;">
          <span style="font-size: 0.85rem; color: #94a3b8;">{{ authService.getUserEmail() }}</span>
          <span class="badge" [ngClass]="authService.isAdmin() ? 'badge-admin' : 'badge-user'">
            {{ authService.getUserRole() }}
          </span>
          <button class="btn btn-secondary btn-sm" (click)="authService.logout()">Sair</button>
        </div>
      </div>
    </nav>
    <main class="container">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .active-link {
      color: #818cf8 !important;
      font-weight: 600 !important;
    }
  `]
})
export class AppComponent {
  constructor(public authService: AuthService) {}
}
