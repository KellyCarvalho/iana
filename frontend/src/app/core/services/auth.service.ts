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

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.tokenKey);
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
