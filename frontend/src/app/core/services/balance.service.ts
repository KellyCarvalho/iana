import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Balance } from '../models/api.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class BalanceService {
  private readonly baseUrl = `${environment.apiUrl}/balances`;

  constructor(private http: HttpClient) {}

  getBalances(): Observable<Balance[]> {
    return this.http.get<Balance[]>(this.baseUrl);
  }

  createBalance(balance: Partial<Balance>): Observable<Balance> {
    return this.http.post<Balance>(this.baseUrl, balance);
  }

  updateBalance(id: string, balance: Partial<Balance>): Observable<Balance> {
    return this.http.put<Balance>(`${this.baseUrl}/${id}`, balance);
  }

  deleteBalance(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
