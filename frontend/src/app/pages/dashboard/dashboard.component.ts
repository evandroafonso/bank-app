import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';

import { Router } from '@angular/router';
import { loadAccounts, Account } from '../../store/accounts/accounts.actions';
import {
  selectAccounts,
  selectAccountsLoading,
  selectAccountsError,
  selectTotalBalance,
} from '../../store/accounts/accounts.selectors';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  accounts$: Observable<Account[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  totalBalance$: Observable<number>;

  constructor(
    private store: Store,
    private router: Router,
  ) {
    this.accounts$ = this.store.select(selectAccounts);
    this.loading$ = this.store.select(selectAccountsLoading);
    this.error$ = this.store.select(selectAccountsError);
    this.totalBalance$ = this.store.select(selectTotalBalance);
  }

  ngOnInit(): void {
    this.store.dispatch(loadAccounts());
  }

  goToAccount(iban: string): void {
    this.router.navigate(['/accounts', iban]);
  }

  formatIban(iban: string): string {
    return iban.replace(/(.{4})/g, '$1 ').trim();
  }
}
