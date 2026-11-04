import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav *ngIf="authService.isLoggedIn()" style="background: linear-gradient(90deg, #1e40af 0%, #2563eb 100%); box-shadow: 0 2px 10px rgba(37, 99, 235, 0.18); padding: 0.9rem 1.5rem;">
      <div style="max-width: 1200px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <div style="display: flex; align-items: center; gap: 2rem;">
          <span style="font-weight: 800; font-size: 1.3rem; color: #ffffff; letter-spacing: -0.02em;">IANA</span>
          <div style="display: flex; gap: 1.25rem;">
            <a routerLink="/finances" routerLinkActive="active-link" style="color: #dbeafe; text-decoration: none; font-weight: 500; padding: 0.3rem 0.65rem; border-radius: 6px;">Finanças</a>
            <a routerLink="/categories" routerLinkActive="active-link" style="color: #dbeafe; text-decoration: none; font-weight: 500; padding: 0.3rem 0.65rem; border-radius: 6px;">Categorias</a>
            <a *ngIf="authService.isAdmin()" routerLink="/users" routerLinkActive="active-link" style="color: #dbeafe; text-decoration: none; font-weight: 500; padding: 0.3rem 0.65rem; border-radius: 6px;">Usuários</a>
          </div>
        </div>
        <div style="display: flex; align-items: center; gap: 0.85rem;">
          <span style="font-size: 0.85rem; color: #e0f2fe; font-weight: 500;">{{ authService.getUserEmail() }}</span>
          <button
            class="btn btn-sm"
            style="background: rgba(255, 255, 255, 0.16); color: #ffffff; border: 1px solid rgba(255, 255, 255, 0.35);"
            (click)="authService.logout()">
            Sair
          </button>
        </div>
      </div>
    </nav>
    <main class="container">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .active-link {
      color: #ffffff !important;
      background: rgba(255, 255, 255, 0.18);
      font-weight: 600 !important;
    }
  `]
})
export class AppComponent {
  constructor(public authService: AuthService) {}
}
