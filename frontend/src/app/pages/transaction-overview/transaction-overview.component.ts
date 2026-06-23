import { CommonModule, Location } from '@angular/common';
import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { Transaction } from '../../store/transactions/transactions.actions';
import {
  clearTransactionDetail,
  loadTransactionDetail,
} from '../../store/transaction-detail/transaction-detail.actions';
import {
  selectTransactionDetail,
  selectTransactionDetailError,
  selectTransactionDetailLoading,
} from '../../store/transaction-detail/transaction-detail.selectors';

@Component({
  selector: 'app-transaction-overview',
  standalone: true,
  imports: [CommonModule, SidebarComponent],
  templateUrl: './transaction-overview.component.html',
  styleUrls: ['./transaction-overview.component.scss'],
})
export class TransactionOverviewComponent implements OnInit, OnDestroy {
  transaction$: Observable<Transaction | null>;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;

  private destroy$ = new Subject<void>();
  private uuid!: string;

  constructor(
    private store: Store,
    private route: ActivatedRoute,
    private location: Location,
  ) {
    this.transaction$ = this.store.select(selectTransactionDetail);
    this.loading$ = this.store.select(selectTransactionDetailLoading);
    this.error$ = this.store.select(selectTransactionDetailError);
  }

  ngOnInit(): void {
    this.uuid = this.route.snapshot.paramMap.get('uuid')!;
    this.store.dispatch(clearTransactionDetail());
    this.store.dispatch(loadTransactionDetail({ uuid: this.uuid }));

    this.error$.pipe(takeUntil(this.destroy$)).subscribe();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.store.dispatch(clearTransactionDetail());
  }

  goBack(): void {
    this.location.back();
  }

  formatDate(timestamp: string): string {
    return new Date(timestamp).toLocaleString('en-US', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  hasCurrencyConversion(sourceCurrency: string, targetCurrency: string): boolean {
    return sourceCurrency !== targetCurrency;
  }
}
