export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  active: boolean;
  emailVerified?: boolean;
  verificationToken?: string;
  verificationLink?: string;
  createdAt: string;
}

export interface Link {
  id: string;
  url: string;
  title: string;
  description: string;
  bookId?: string;
  categoryId?: string;
  active: boolean;
  createdAt: string;
}

export interface Category {
  id: string;
  name: string;
  description: string;
  links?: Link[];
}

export interface Expense {
  id: string;
  name: string;
  amount: number;
  description?: string;
  categoryId?: string;
  createdAt: string;
}

export interface Balance {
  id: string;
  source: string;
  amount: number;
  description?: string;
  createdAt: string;
}

export interface ImportFinancesResponse {
  expenses: Expense[];
  balances: Balance[];
  totalExpensesImported: number;
  totalBalancesImported: number;
}
