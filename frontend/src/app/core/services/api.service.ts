import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Balance, Category, Expense, ImportFinancesResponse, Link, User } from '../models/api.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly baseUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Expenses (Despesas)
  getExpenses(categoryId?: string): Observable<Expense[]> {
    let params = new HttpParams();
    if (categoryId) params = params.set('categoryId', categoryId);
    return this.http.get<Expense[]>(`${this.baseUrl}/expenses`, { params });
  }

  createExpense(expense: Partial<Expense>): Observable<Expense> {
    return this.http.post<Expense>(`${this.baseUrl}/expenses`, expense);
  }

  updateExpense(id: string, expense: Partial<Expense>): Observable<Expense> {
    return this.http.put<Expense>(`${this.baseUrl}/expenses/${id}`, expense);
  }

  deleteExpense(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/expenses/${id}`);
  }

  importExpensesCsv(file: File, categoryId?: string): Observable<ImportFinancesResponse> {
    const formData = new FormData();
    formData.append('file', file);
    if (categoryId) {
      formData.append('categoryId', categoryId);
    }
    return this.http.post<ImportFinancesResponse>(`${this.baseUrl}/expenses/import-csv`, formData);
  }

  // Balances (Saldos)
  getBalances(): Observable<Balance[]> {
    return this.http.get<Balance[]>(`${this.baseUrl}/balances`);
  }

  createBalance(balance: Partial<Balance>): Observable<Balance> {
    return this.http.post<Balance>(`${this.baseUrl}/balances`, balance);
  }

  updateBalance(id: string, balance: Partial<Balance>): Observable<Balance> {
    return this.http.put<Balance>(`${this.baseUrl}/balances/${id}`, balance);
  }

  deleteBalance(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/balances/${id}`);
  }

  // Categories
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`);
  }

  createCategory(category: Partial<Category>): Observable<Category> {
    return this.http.post<Category>(`${this.baseUrl}/categories`, category);
  }

  updateCategory(id: string, category: Partial<Category>): Observable<Category> {
    return this.http.put<Category>(`${this.baseUrl}/categories/${id}`, category);
  }

  deleteCategory(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/categories/${id}`);
  }

  // Links
  getLinks(bookId?: string, categoryId?: string): Observable<Link[]> {
    let params = new HttpParams();
    if (bookId) params = params.set('bookId', bookId);
    if (categoryId) params = params.set('categoryId', categoryId);
    return this.http.get<Link[]>(`${this.baseUrl}/links`, { params });
  }

  createLink(link: Partial<Link>): Observable<Link> {
    return this.http.post<Link>(`${this.baseUrl}/links`, link);
  }

  updateLink(id: string, link: Partial<Link>): Observable<Link> {
    return this.http.put<Link>(`${this.baseUrl}/links/${id}`, link);
  }

  deleteLink(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/links/${id}`);
  }

  // Users
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.baseUrl}/users`);
  }

  updateUser(id: string, user: any): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/users/${id}`, user);
  }

  updateUserRole(id: string, role: string): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/users/${id}/role`, { role });
  }

  deleteUser(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/users/${id}`);
  }
}
