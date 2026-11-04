import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div style="max-width: 440px; margin: 3rem auto;">
      <div class="card" style="border-top: 4px solid #2563eb;">
        <h1 style="font-size: 1.6rem; margin-bottom: 0.25rem; text-align: center; color: #1e3a8a; font-weight: 800;">IANA</h1>
        <p style="color: #475569; font-size: 0.9rem; text-align: center; margin-bottom: 1.5rem;">
          Organizador de Finanças
        </p>

        <div style="display: flex; gap: 0.5rem; margin-bottom: 1.5rem; background: #eff6ff; padding: 0.35rem; border-radius: 10px; border: 1px solid #dbeafe;">
          <button class="btn" style="flex: 1;" [ngClass]="mode === 'login' ? 'btn-primary' : 'btn-secondary'" (click)="setMode('login')">Login</button>
          <button class="btn" style="flex: 1;" [ngClass]="mode === 'register' ? 'btn-primary' : 'btn-secondary'" (click)="setMode('register')">Criar Conta</button>
        </div>

        <div *ngIf="errorMessage" class="alert alert-error">{{ errorMessage }}</div>
        <div *ngIf="successMessage" class="alert alert-success">{{ successMessage }}</div>

        <!-- LOGIN -->
        <form *ngIf="mode === 'login'" (ngSubmit)="onLogin()" style="display: flex; flex-direction: column; gap: 1rem;">
          <div class="form-group">
            <label>E-mail</label>
            <input type="email" [(ngModel)]="email" name="email" placeholder="seu&#64;email.com" required />
          </div>
          <div class="form-group">
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <label>Senha</label>
              <a routerLink="/forgot-password" style="color: #2563eb; font-size: 0.82rem; text-decoration: none; font-weight: 600;">
                Esqueci minha senha
              </a>
            </div>
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
            <input type="email" [(ngModel)]="email" name="email" placeholder="seu&#64;email.com" required />
          </div>
          <div class="form-group">
            <label>Senha *</label>
            <input type="password" [(ngModel)]="password" name="password" placeholder="Ex: Senha#123" required />
            <div style="margin-top: 0.45rem; font-size: 0.78rem; display: flex; flex-direction: column; gap: 0.2rem;">
              <span [style.color]="hasMinLength() ? '#059669' : '#64748b'">
                {{ hasMinLength() ? '✓' : '○' }} Mínimo de 8 caracteres
              </span>
              <span [style.color]="hasUppercase() ? '#059669' : '#64748b'">
                {{ hasUppercase() ? '✓' : '○' }} Pelo menos 1 letra maiúscula (A-Z)
              </span>
              <span [style.color]="hasNumber() ? '#059669' : '#64748b'">
                {{ hasNumber() ? '✓' : '○' }} Pelo menos 1 número (0-9)
              </span>
              <span [style.color]="hasSpecialChar() ? '#059669' : '#64748b'">
                {{ hasSpecialChar() ? '✓' : '○' }} Pelo menos 1 caractere especial (!#$%...)
              </span>
            </div>
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Cadastrar</button>
        </form>

        <!-- VERIFY EMAIL -->
        <form *ngIf="mode === 'verify'" (ngSubmit)="onVerifyEmail()" style="display: flex; flex-direction: column; gap: 1rem;">
          <p style="color: #334155; font-size: 0.88rem; text-align: center; margin: 0;">
            Digite abaixo o <strong>código de 6 dígitos</strong> enviado para o seu e-mail{{ email ? ' (' + email + ')' : '' }}:
          </p>
          <div class="form-group">
            <label>Código de Verificação (6 dígitos) *</label>
            <input
              type="text"
              [(ngModel)]="verificationToken"
              name="verificationToken"
              maxlength="6"
              placeholder="Digite os 6 dígitos"
              autocomplete="one-time-code"
              style="text-align: center; font-size: 1.35rem; font-weight: 700; letter-spacing: 0.35rem; color: #1e3a8a;"
              required />
          </div>
          <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 0.5rem;">Validar Código e Ativar Conta</button>
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

  hasMinLength(): boolean {
    return (this.password || '').length >= 8;
  }

  hasUppercase(): boolean {
    return /[A-Z]/.test(this.password || '');
  }

  hasNumber(): boolean {
    return /\d/.test(this.password || '');
  }

  hasSpecialChar(): boolean {
    return /[^A-Za-z0-9]/.test(this.password || '');
  }

  isPasswordValid(): boolean {
    return this.hasMinLength() && this.hasUppercase() && this.hasNumber() && this.hasSpecialChar();
  }

  onLogin(): void {
    this.errorMessage = '';
    this.authService.login(this.email, this.password).subscribe({
      next: () => this.router.navigate(['/finances']),
      error: (err) => {
        this.errorMessage = err.error?.message || 'Falha ao autenticar. Verifique suas credenciais.';
      }
    });
  }

  onRegister(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.isPasswordValid()) {
      this.errorMessage = 'A senha deve conter no mínimo 8 caracteres, incluindo pelo menos uma letra maiúscula, um número e um caractere especial.';
      return;
    }
    this.authService.register(this.name, this.email, this.password).subscribe({
      next: () => {
        this.verificationToken = '';
        this.mode = 'verify';
        this.successMessage = `Enviamos um código de verificação de 6 dígitos para o e-mail ${this.email}. Digite o código no campo abaixo para confirmar seu cadastro!`;
      },
      error: (err) => {
        this.errorMessage =
          err.error?.fieldErrors?.password ||
          err.error?.message ||
          'Erro ao cadastrar usuário.';
      }
    });
  }

  onVerifyEmail(): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.authService.verifyEmail(this.verificationToken.trim()).subscribe({
      next: () => {
        this.mode = 'login';
        this.successMessage = 'Código confirmado e conta ativada com sucesso! Agora você já pode fazer login.';
      },
      error: (err) => {
        this.errorMessage = err.error?.message || 'Código inválido ou já utilizado.';
      }
    });
  }
}
