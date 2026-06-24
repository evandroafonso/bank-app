import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';
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
import { AccountsService } from '../../services/accounts.service';
import { CurrenciesService, Currency } from '../../services/currencies.service';
import { getApiErrorMessage } from '../../utils/api-error.util';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SidebarComponent],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  accounts$: Observable<Account[]>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  totalBalance$: Observable<number>;
  createAccountForm: FormGroup;
  currencies: Currency[] = [];
  currenciesLoading = false;
  createAccountLoading = false;
  createAccountError: string | null = null;
  createAccountSuccess: string | null = null;
  isCreateAccountModalOpen = false;

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private router: Router,
    private accountsService: AccountsService,
    private currenciesService: CurrenciesService,
  ) {
    this.accounts$ = this.store.select(selectAccounts);
    this.loading$ = this.store.select(selectAccountsLoading);
    this.error$ = this.store.select(selectAccountsError);
    this.totalBalance$ = this.store.select(selectTotalBalance);
    this.createAccountForm = this.fb.group({
      currency: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.store.dispatch(loadAccounts());
    this.loadCurrencies();
  }

  goToAccount(iban: string): void {
    this.router.navigate(['/accounts', iban]);
  }

  openCreateAccountModal(): void {
    this.isCreateAccountModalOpen = true;
    this.createAccountError = null;
    this.createAccountSuccess = null;

    if (!this.currencies.length) {
      this.loadCurrencies();
    }
  }

  closeCreateAccountModal(): void {
    if (this.createAccountLoading) {
      return;
    }

    this.isCreateAccountModalOpen = false;
    this.createAccountForm.reset();
    this.createAccountError = null;
    this.createAccountSuccess = null;
  }

  formatIban(iban: string): string {
    return iban.replace(/(.{4})/g, '$1 ').trim();
  }

  createAccount(): void {
    if (this.createAccountForm.invalid || this.createAccountLoading) {
      this.createAccountForm.markAllAsTouched();
      return;
    }

    const currency = this.createAccountForm.getRawValue().currency;

    this.createAccountLoading = true;
    this.createAccountError = null;
    this.createAccountSuccess = null;

    this.accountsService
      .createAccount(currency)
      .pipe(finalize(() => (this.createAccountLoading = false)))
      .subscribe({
        next: (account) => {
          this.createAccountSuccess = `Account ${this.formatIban(account.iban)} created successfully.`;
          this.createAccountForm.reset();
          this.store.dispatch(loadAccounts());
          this.isCreateAccountModalOpen = false;
        },
        error: (error) => {
          this.createAccountError = getApiErrorMessage(
            error,
            'Unable to create account. Please try again.',
          );
        },
      });
  }

  private loadCurrencies(): void {
    this.currenciesLoading = true;

    this.currenciesService
      .getCurrencies()
      .pipe(finalize(() => (this.currenciesLoading = false)))
      .subscribe({
        next: (currencies) => {
          this.currencies = currencies;
        },
        error: (error) => {
          this.createAccountError = getApiErrorMessage(
            error,
            'Unable to load currencies. Please try again.',
          );
        },
      });
  }
}
