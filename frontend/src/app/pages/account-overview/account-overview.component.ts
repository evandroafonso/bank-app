import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ElementRef,
  ViewChild,
  NgZone,
  ChangeDetectorRef,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { EMPTY, Observable } from 'rxjs';
import { catchError, finalize, take, timeout } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import {
  loadAccountDetail,
  clearAccountDetail,
} from '../../store/account-detail/account-detail.actions';
import {
  selectAccountDetail,
  selectAccountDetailLoading,
  selectAccountDetailError,
} from '../../store/account-detail/account-detail.selectors';
import { Account } from '../../store/accounts/accounts.actions';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import {
  loadTransactions,
  clearTransactions,
  Transaction,
} from '../../store/transactions/transactions.actions';
import {
  selectTransactions,
  selectTransactionsLoading,
  selectTransactionsError,
  selectIsLastPage,
  selectNextPage,
  selectTotalElements,
} from '../../store/transactions/transactions.selectors';
import { TransactionsService, TransactionOperationType } from '../../services/transactions.service';
import { getApiErrorMessage } from '../../utils/api-error.util';

const PAGE_SIZE = 5;

@Component({
  selector: 'app-account-overview',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, SidebarComponent],
  templateUrl: '../account-overview/account-overview.component.html',
  styleUrls: ['../account-overview/account-overview.component.scss'],
})
export class AccountOverviewComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('scrollSentinel') scrollSentinel!: ElementRef;

  account$: Observable<Account | null>;
  accountLoading$: Observable<boolean>;
  accountError$: Observable<string | null>;
  transactions$: Observable<Transaction[]>;
  transactionsLoading$: Observable<boolean>;
  transactionsError$: Observable<string | null>;
  isLastPage$: Observable<boolean>;
  totalElements$: Observable<number>;

  operationForm: FormGroup;
  operationLoading = false;
  operationError: string | null = null;
  operationSuccess: string | null = null;
  toastMessage: string | null = null;
  toastType: 'success' | 'error' = 'success';

  private iban!: string;
  private observer!: IntersectionObserver;
  private toastTimeoutId: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private store: Store,
    private route: ActivatedRoute,
    private router: Router,
    private ngZone: NgZone,
    private cdr: ChangeDetectorRef,
    private fb: FormBuilder,
    private transactionsService: TransactionsService,
  ) {
    this.operationForm = this.fb.group({
      type: ['CREDIT' as TransactionOperationType, Validators.required],
      amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
      currency: ['EUR', [Validators.required, Validators.minLength(3), Validators.maxLength(3)]],
      description: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
    });

    this.account$ = this.store.select(selectAccountDetail);
    this.accountLoading$ = this.store.select(selectAccountDetailLoading);
    this.accountError$ = this.store.select(selectAccountDetailError);
    this.transactions$ = this.store.select(selectTransactions);
    this.transactionsLoading$ = this.store.select(selectTransactionsLoading);
    this.transactionsError$ = this.store.select(selectTransactionsError);
    this.isLastPage$ = this.store.select(selectIsLastPage);
    this.totalElements$ = this.store.select(selectTotalElements);
  }

  ngOnInit(): void {
    this.iban = this.route.snapshot.paramMap.get('iban')!;
    this.store.dispatch(loadAccountDetail({ iban: this.iban }));
    this.store.dispatch(loadTransactions({ iban: this.iban, page: 0, size: PAGE_SIZE }));
  }

  ngAfterViewInit(): void {
    this.observer = new IntersectionObserver(
      (entries) => {
        const entry = entries[0];
        if (!entry.isIntersecting) return;

        this.ngZone.run(() => {
          this.store
            .select(selectIsLastPage)
            .pipe(take(1))
            .subscribe((isLast) => {
              if (isLast) return;
              this.store
                .select(selectTransactionsLoading)
                .pipe(take(1))
                .subscribe((loading) => {
                  if (loading) return;
                  this.store
                    .select(selectNextPage)
                    .pipe(take(1))
                    .subscribe((nextPage) => {
                      this.store.dispatch(
                        loadTransactions({ iban: this.iban, page: nextPage, size: PAGE_SIZE }),
                      );
                    });
                });
            });
        });
      },
      { threshold: 0.1 },
    );

    if (this.scrollSentinel?.nativeElement) {
      this.observer.observe(this.scrollSentinel.nativeElement);
    }
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
    if (this.toastTimeoutId) {
      clearTimeout(this.toastTimeoutId);
    }
    this.store.dispatch(clearAccountDetail());
    this.store.dispatch(clearTransactions());
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  goToTransaction(uuid: string): void {
    this.router.navigate(['/transactions', uuid]);
  }

  submitOperation(): void {
    if (this.operationForm.invalid || !this.iban || this.operationLoading) {
      this.operationForm.markAllAsTouched();
      return;
    }

    const rawValue = this.operationForm.getRawValue();
    const payload = {
      iban: this.iban,
      amount: Number(rawValue.amount),
      description: rawValue.description?.trim() ?? '',
      currency: rawValue.currency?.trim().toUpperCase() ?? 'EUR',
    };

    if (!payload.description) {
      this.operationError = 'Description is required.';
      return;
    }

    this.setOperationLoading(true);
    this.operationError = null;
    this.operationSuccess = null;

    const operation$ =
      rawValue.type === 'DEBIT'
        ? this.transactionsService.debitAccount(payload)
        : this.transactionsService.creditAccount(payload);

    operation$
      .pipe(
        timeout(15000),
        catchError((error) => {
          this.handleOperationError(error, rawValue.type);
          return EMPTY;
        }),
        finalize(() => this.setOperationLoading(false)),
      )
      .subscribe(() => {
        this.setOperationLoading(false);
        this.operationSuccess =
          rawValue.type === 'DEBIT'
            ? 'Debit completed successfully.'
            : 'Credit completed successfully.';
        this.showToast(this.operationSuccess, 'success');
        this.store.dispatch(loadAccountDetail({ iban: this.iban }));
        this.store.dispatch(clearTransactions());
        this.store.dispatch(loadTransactions({ iban: this.iban, page: 0, size: PAGE_SIZE }));
      });
  }

  setOperationType(type: TransactionOperationType): void {
    this.operationForm.patchValue({ type });
    this.operationError = null;
    this.operationSuccess = null;
  }

  private getOperationErrorMessage(
    error: unknown,
    operationType: TransactionOperationType,
  ): string {
    const fallbackMessage =
      operationType === 'DEBIT'
        ? 'Unable to debit money from this account.'
        : 'Unable to add money to this account.';

    return getApiErrorMessage(error, fallbackMessage);
  }

  private handleOperationError(error: unknown, operationType: TransactionOperationType): void {
    this.ngZone.run(() => {
      this.setOperationLoading(false);
      this.operationError = this.getOperationErrorMessage(error, operationType);
      this.showToast(this.operationError, 'error');
    });
  }

  private setOperationLoading(isLoading: boolean): void {
    this.operationLoading = isLoading;
    this.cdr.detectChanges();
  }

  private showToast(message: string, type: 'success' | 'error'): void {
    this.ngZone.run(() => {
      this.toastMessage = message;
      this.toastType = type;
      this.cdr.detectChanges();
    });

    if (this.toastTimeoutId) {
      clearTimeout(this.toastTimeoutId);
    }

    this.toastTimeoutId = setTimeout(() => {
      this.ngZone.run(() => {
        this.toastMessage = null;
        this.toastTimeoutId = null;
        this.cdr.detectChanges();
      });
    }, 5000);
  }

  formatIban(iban: string): string {
    return iban.replace(/(.{4})/g, '$1 ').trim();
  }

  formatDate(timestamp: string): string {
    return new Date(timestamp).toLocaleString('pt-BR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
