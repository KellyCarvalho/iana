import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div style="max-width: 440px; margin: 3rem auto;">
      <div class="card" style="border-top: 4px solid #2563eb;">
        <h1 style="font-size: 1.5rem; margin-bottom: 0.35rem; text-align: center; color: #1e3a8a; font-weight: 800;">Recuperar Senha</h1>
        <p style="color: #475569; font-size: 0.9rem; text-align: center; margin-bottom: 1.5rem;">
          IANA • Verificação por E-mail
        </p>

        <div *ngIf="errorMessage" class="alert alert-error">{{ errorMessage }}</div>
        <div *ngIf="successMessage" class="alert alert-success">{{ successMessage }}</div>

        <!-- ETAPA 1: Solicitar código por e-mail -->
        <form *ngIf="step === 'request'" (ngSubmit)="onRequestCode()" style="display: flex; flex-direction: column; gap: 1rem;">
          <p style="color: #334155; font-size: 0.88rem; margin: 0;">
            Informe o e-mail da sua conta para receber um <strong>código de verificação de 6 dígitos</strong>.
          </p>
          <div class="form-group">
            <label>E-mail *</label>
            <input
              type="email"
              [(ngModel)]="email"
              name="email"
              placeholder="seu&#64;email.com"
              required />
          </div>
          <button type="submit" class="btn btn-primary" [disabled]="loading" style="width: 100%; margin-top: 0.5rem;">
            {{ loading ? 'Enviando código...' : 'Enviar Código de Verificação' }}
          </button>
        </form>

        <!-- ETAPA 2 e 3: Digitar código recebido por e-mail e, após validado, exibir campos de Nova Senha e Repita a Senha -->
        <div *ngIf="step === 'verify' || step === 'reset'" style="display: flex; flex-direction: column; gap: 1rem;">
          <div class="form-group">
            <label>E-mail</label>
            <input type="email" [value]="email" disabled style="opacity: 0.75; cursor: not-allowed; background: #eff6ff;" />
          </div>

          <!-- Validação do código -->
          <form *ngIf="!codeVerified" (ngSubmit)="onVerifyCode()" style="display: flex; flex-direction: column; gap: 1rem;">
            <p style="color: #334155; font-size: 0.88rem; text-align: center; margin: 0;">
              Digite o <strong>código de 6 dígitos</strong> enviado para <strong>{{ email }}</strong>:
            </p>
            <div class="form-group">
              <label>Código de Verificação (6 dígitos) *</label>
              <input
                type="text"
                [(ngModel)]="code"
                name="code"
                maxlength="6"
                placeholder="000000"
                autocomplete="one-time-code"
                style="text-align: center; font-size: 1.35rem; font-weight: 700; letter-spacing: 0.35rem; color: #1e3a8a;"
                required />
            </div>
            <button type="submit" class="btn btn-primary" [disabled]="loading || code.trim().length !== 6" style="width: 100%;">
              {{ loading ? 'Verificando...' : 'Verificar Código' }}
            </button>
            <button type="button" class="btn btn-secondary" (click)="onResendCode()" [disabled]="loading" style="width: 100%; font-size: 0.85rem;">
              Reenviar código para o e-mail
            </button>
          </form>

          <!-- Campos de Nova Senha e Repita a Senha (aparecem na tela após o código ser validado) -->
          <form *ngIf="codeVerified" (ngSubmit)="onResetPassword()" style="display: flex; flex-direction: column; gap: 1rem;">
            <div style="display: flex; align-items: center; justify-content: space-between; background: #ecfdf5; border: 1px solid #a7f3d0; padding: 0.6rem 0.85rem; border-radius: 8px; font-size: 0.85rem; color: #047857; font-weight: 600;">
              <span>✓ Código {{ code }} verificado com sucesso</span>
            </div>

            <div class="form-group">
              <label>Nova Senha *</label>
              <input
                type="password"
                [(ngModel)]="newPassword"
                name="newPassword"
                placeholder="Ex: NovaSenha#123"
                required />
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

            <div class="form-group">
              <label>Repita a Senha *</label>
              <input
                type="password"
                [(ngModel)]="confirmPassword"
                name="confirmPassword"
                placeholder="Repita a nova senha"
                required />
              <div *ngIf="confirmPassword.length > 0" style="margin-top: 0.35rem; font-size: 0.78rem; font-weight: 500;">
                <span [style.color]="passwordsMatch() ? '#059669' : '#dc2626'">
                  {{ passwordsMatch() ? '✓ As senhas coincidem' : '✗ As senhas não coincidem' }}
                </span>
              </div>
            </div>

            <button
              type="submit"
              class="btn btn-primary"
              [disabled]="loading || !isPasswordValid() || !passwordsMatch()"
              style="width: 100%; margin-top: 0.5rem;">
              {{ loading ? 'Salvando nova senha...' : 'Redefinir Senha' }}
            </button>
          </form>
        </div>

        <!-- ETAPA 4: Concluído -->
        <div *ngIf="step === 'done'" style="display: flex; flex-direction: column; gap: 1rem; text-align: center;">
          <p style="color: #334155; font-size: 0.92rem; margin: 0;">
            Sua senha foi atualizada! Você já pode acessar sua conta com a nova senha.
          </p>
          <a routerLink="/login" class="btn btn-primary" style="text-decoration: none; display: inline-block; width: 100%;">
            Ir para o Login
          </a>
        </div>

        <div *ngIf="step !== 'done'" style="margin-top: 1.5rem; text-align: center; border-top: 1px solid #dbeafe; padding-top: 1rem;">
          <a routerLink="/login" style="color: #2563eb; font-size: 0.85rem; text-decoration: none; font-weight: 600;">
            ← Voltar para o Login
          </a>
        </div>
      </div>
    </div>
  `
})
export class ForgotPasswordComponent {
  step: 'request' | 'verify' | 'reset' | 'done' = 'request';
  email = '';
  code = '';
  newPassword = '';
  confirmPassword = '';
  codeVerified = false;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private authService: AuthService, private router: Router) {}

  hasMinLength(): boolean {
    return (this.newPassword || '').length >= 8;
  }

  hasUppercase(): boolean {
    return /[A-Z]/.test(this.newPassword || '');
  }

  hasNumber(): boolean {
    return /\d/.test(this.newPassword || '');
  }

  hasSpecialChar(): boolean {
    return /[^A-Za-z0-9]/.test(this.newPassword || '');
  }

  isPasswordValid(): boolean {
    return this.hasMinLength() && this.hasUppercase() && this.hasNumber() && this.hasSpecialChar();
  }

  passwordsMatch(): boolean {
    return (this.newPassword || '').length > 0 && this.newPassword === this.confirmPassword;
  }

  onRequestCode(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.email.trim()) {
      this.errorMessage = 'Informe o seu e-mail.';
      return;
    }
    this.loading = true;
    this.authService.forgotPassword(this.email.trim()).subscribe({
      next: (res) => {
        this.loading = false;
        this.step = 'verify';
        this.codeVerified = false;
        this.code = '';
        this.successMessage = res.message || `Enviamos um código de 6 dígitos para ${this.email}.`;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Não foi possível enviar o código de verificação.';
      }
    });
  }

  onResendCode(): void {
    this.onRequestCode();
  }

  onVerifyCode(): void {
    this.errorMessage = '';
    this.successMessage = '';
    const trimmedCode = this.code.trim();
    if (trimmedCode.length !== 6) {
      this.errorMessage = 'O código deve conter 6 dígitos.';
      return;
    }
    this.loading = true;
    this.authService.verifyResetCode(this.email.trim(), trimmedCode).subscribe({
      next: () => {
        this.loading = false;
        this.codeVerified = true;
        this.step = 'reset';
        this.successMessage = 'Código confirmado! Agora escolha sua nova senha abaixo.';
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Código de verificação inválido.';
      }
    });
  }

  onResetPassword(): void {
    this.errorMessage = '';
    this.successMessage = '';
    if (!this.isPasswordValid()) {
      this.errorMessage = 'A nova senha deve atender a todos os critérios de segurança.';
      return;
    }
    if (!this.passwordsMatch()) {
      this.errorMessage = 'A confirmação de senha não confere com a nova senha.';
      return;
    }
    this.loading = true;
    this.authService.resetPassword({
      email: this.email.trim(),
      code: this.code.trim(),
      newPassword: this.newPassword,
      confirmPassword: this.confirmPassword
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.step = 'done';
        this.successMessage = res.message || 'Senha redefinida com sucesso!';
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err.error?.fieldErrors?.newPassword ||
          err.error?.message ||
          'Erro ao redefinir a senha.';
      }
    });
  }
}
