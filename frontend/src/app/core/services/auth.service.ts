import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { AuthResponse, User } from '../models/api.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;
  private readonly tokenKey = 'iana_token';

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, { email, password }).pipe(
      tap((res) => {
        localStorage.setItem(this.tokenKey, res.accessToken);
      })
    );
  }

  register(name: string, email: string, password: string): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/register`, { name, email, password });
  }

  verifyEmail(token: string): Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/verify-email`, { token });
  }

  forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/forgot-password`, { email });
  }

  verifyResetCode(email: string, code: string): Observable<{ valid: boolean; message: string }> {
    return this.http.post<{ valid: boolean; message: string }>(`${this.apiUrl}/verify-reset-code`, { email, code });
  }

  resetPassword(payload: { email: string; code: string; newPassword: string; confirmPassword: string }): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/reset-password`, payload);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    const token = localStorage.getItem(this.tokenKey);
    if (!token) return false;
    const payload = this.getDecodedToken();
    if (!payload) {
      localStorage.removeItem(this.tokenKey);
      return false;
    }
    if (payload.exp && Date.now() >= payload.exp * 1000) {
      localStorage.removeItem(this.tokenKey);
      return false;
    }
    return true;
  }

  getUserEmail(): string {
    const payload = this.getDecodedToken();
    return payload?.sub || '';
  }

  getUserRole(): string {
    const payload = this.getDecodedToken();
    return payload?.role || 'ROLE_USER';
  }

  isAdmin(): boolean {
    const role = this.getUserRole();
    return role === 'ROLE_ADMIN' || role === 'ADMIN';
  }

  private getDecodedToken(): any {
    const token = localStorage.getItem(this.tokenKey);
    if (!token) return null;
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      return JSON.parse(atob(parts[1]));
    } catch {
      return null;
    }
  }
}
