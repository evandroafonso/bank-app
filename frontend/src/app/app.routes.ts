import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/dashboard/dashboard.component').then((m) => m.DashboardComponent),
  },
  {
    path: 'accounts/:iban',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/account-overview/account-overview.component').then(
        (m) => m.AccountOverviewComponent,
      ),
  },
  {
    path: 'transactions/:uuid',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/transaction-overview/transaction-overview.component').then(
        (m) => m.TransactionOverviewComponent,
      ),
  },
  { path: '**', redirectTo: 'login' },
];
