import { Routes } from '@angular/router';
import { adminGuard, authGuard } from './core/guards/auth.guard';
import { AuthComponent } from './pages/auth/auth.component';
import { ForgotPasswordComponent } from './pages/auth/forgot-password.component';
import { CategoriesComponent } from './pages/categories/categories.component';
import { FinancesComponent } from './pages/finances/finances.component';
import { UsersComponent } from './pages/users/users.component';

export const routes: Routes = [
  { path: 'login', component: AuthComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'finances', component: FinancesComponent, canActivate: [authGuard] },
  { path: 'expenses', component: FinancesComponent, canActivate: [authGuard], data: { tab: 'expenses' } },
  { path: 'balances', component: FinancesComponent, canActivate: [authGuard], data: { tab: 'balances' } },
  { path: 'categories', component: CategoriesComponent, canActivate: [authGuard] },
  { path: 'users', component: UsersComponent, canActivate: [authGuard, adminGuard] },
  { path: '', redirectTo: 'finances', pathMatch: 'full' },
  { path: '**', redirectTo: 'finances' }
];
