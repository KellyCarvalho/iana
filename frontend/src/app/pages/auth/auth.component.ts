import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div style="max-width: 440px; margin: 3rem auto;">
      <div class="card">
        <h1 style="font-size: 1.5rem; margin-bottom: 0.35rem; text-align: center;">IANA</h1>
        <p style="color: #94a3b8; font-size: 0.9rem; text-align: center; margin-bottom: 1.5rem;">
          Organizador de Links & Livros
        </p>

        <div style="display: flex; gap: 0.5rem; margin-bottom: 1.5rem; background: #0f172a; padding: 0.3rem; border-radius: 8px;">
          <button class="btn" style="flex: 1;" [ngClass]="mode === 'login' ? 'btn-primary' : 'btn-secondary'" (click)="setMode('login')">Login</button>
          <button class="btn" style="flex: 1;" [ngClass]="mode === 'register' ? 'btn-primary' : 'btn-secondary'" (click)="setMode('register')">Criar Conta</button>
          <button class="btn" style="flex: 1;" [ngClass]="mode === 'verify' ? 'btn-primary' : 'btn-secondary'" (click)="setMode('verify')">Verificar E-mail</button>
        </div>

        <div *ngIf="errorMessage" class="alert alert-error">{{ errorMessage }}</div>
        <div *ngIf="successMessage" class="alert alert-success">{{ successMessage }}</div>

        <!-- LOGIN -->
        <form *ngIf="mode === 'login'" (ngSubmit)="onLogin()" style="display: flex; flex-direction: column; gap: 1rem;">
          <div class="form-group">
            <label>E-mail</label>
            <input type="email" [(ngModel)]="email" name="email" placeholder="seu@email.com" required />
          </div>
          <div class="form-group">
            <label>Senha</label>
            <input type="password" [(ngModel)]="password" name="password" placeholder="••••••" required />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Entrar</button>
        </form>

        <!-- REGISTER -->
        <form *ngIf="mode === 'register'" (ngSubmit)="onRegister()" style="display: flex; flex-direction: column; gap: 1rem;">
          <div class="form-group">
            <label>Nome</label>
            <input type="text" [(ngModel)]="name" name="name" placeholder="Seu nome" required />
          </div>
          <div class="form-group">
            <label>E-mail</label>
            <input type="email" [(ngModel)]="email" name="email" placeholder="seu@email.com" required />
          </div>
          <div class="form-group">
            <label>Senha (mín. 6 caracteres)</label>
            <input type="password" [(ngModel)]="password" name="password" placeholder="••••••" required />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Cadastrar (ROLE_USER)</button>
        </form>

        <!-- VERIFY EMAIL -->
        <form *ngIf="mode === 'verify'" (ngSubmit)="onVerifyEmail()" style="display: flex; flex-direction: column; gap: 1rem;">
          <div class="form-group">
            <label>Token de Verificação de E-mail</label>
            <input type="text" [(ngModel)]="verificationToken" name="verificationToken" placeholder="Cole o token gerado no cadastro" required />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Confirmar E-mail e Ativar Conta</button>
        </form>
      </div>
    </div>
  `
})
export class AuthComponent {
  mode: 'login' | 'register' | 'verify' = 'login';
  name = '';
  email = '';
  password = '';
  verificationToken = '';
  errorMessage = '';
  successMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  setMode(newMode: 'login' | 'register' | 'verify'): void {
    this.mode = newMode;
    this.errorMessage = '';
    this.successMessage = '';
  }

  onLogin(): void {
    this.errorMessage = '';
    this.authService.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/books']),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Falha ao autenticar. Verifique suas credenciais.';
      }
    });
  }

  onRegister(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.register(this.name, this.email, this.password).subscribe({
      next: (user) => {
        this.verificationToken = user.verificationToken || '';
        this.mode = 'verify';
        this.successMessage = `Usuário criado com ROLE_USER! Token de verificação preenchido abaixo — clique em Confirmar E-mail para ativar a conta.`;
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Erro ao cadastrar usuário.';
      }
    });
  }

  onVerifyEmail(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.verifyEmail(this.verificationToken).subscribe({
      next: () => {
        this.mode = 'login';
        this.successMessage = 'E-mail confirmado e conta ativada com sucesso! Agora você já pode fazer login.';
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Token inválido ou já utilizado.';
      }
    });
  }
}
