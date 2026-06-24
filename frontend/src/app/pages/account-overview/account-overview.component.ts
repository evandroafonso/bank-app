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

import { Observable, Subject } from 'rxjs';

import { filter, finalize, take, takeUntil, timeout } from 'rxjs/operators';

import { CommonModule } from '@angular/common';

import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

import {
  loadAccountDetail,
  clearAccountDetail,
} from '../../store/account-overview/account-detail.actions';

import {
  selectAccountDetail,
  selectAccountDetailLoading,
  selectAccountDetailError,
} from '../../store/account-overview/account-detail.selectors';

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

import { CurrenciesService, Currency } from '../../services/currencies.service';

import { SmartCurrencyPipe } from '../../pipes/smart-currency-pipe';

const PAGE_SIZE = 5;

@Component({
  selector: 'app-account-overview',

  standalone: true,

  imports: [CommonModule, ReactiveFormsModule, SidebarComponent, SmartCurrencyPipe],

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

  chartFilterForm: FormGroup;

  operationLoading = false;

  operationError: string | null = null;

  operationSuccess: string | null = null;

  toastMessage: string | null = null;

  toastType: 'success' | 'error' = 'success';

  currencies: Currency[] = [];

  currenciesLoading = false;

  currenciesError: string | null = null;

  // Balance chart

  balanceChartData: Array<{ timestamp: string; balance: number }> = [];

  balanceChartLoading = false;

  balanceChartError: string | null = null;

  balanceChartPath: string | null = null;

  balanceChartPoints: Array<{
    x: number;
    y: number;
    label: string;
    balance: number;
    balanceLabel: string;
  }> = [];

  balanceChartXTicks: Array<{ x: number; label: string }> = [];

  balanceChartYTicks: Array<{ y: number; label: string }> = [];

  hoveredChartPoint: AccountOverviewComponent['balanceChartPoints'][number] | null = null;

  balanceChartWidth = 720;

  balanceChartHeight = 320;

  balanceChartPaddingLeft = 64;

  balanceChartPaddingRight = 20;

  balanceChartPaddingTop = 20;

  balanceChartPaddingBottom = 44;

  private balanceChartRequestId = 0;

  private iban!: string;

  private observer!: IntersectionObserver;

  private toastTimeoutId: ReturnType<typeof setTimeout> | null = null;

  private destroy$ = new Subject<void>();

  private accountCurrency: string | null = null;

  constructor(
    private store: Store,

    private route: ActivatedRoute,

    private router: Router,

    private ngZone: NgZone,

    private cdr: ChangeDetectorRef,

    private fb: FormBuilder,

    private transactionsService: TransactionsService,

    private currenciesService: CurrenciesService,
  ) {
    this.operationForm = this.fb.group({
      type: ['CREDIT' as TransactionOperationType, Validators.required],

      amount: [null as number | null, [Validators.required, Validators.min(0.01)]],

      currency: ['', Validators.required],

      description: ['', [Validators.minLength(3), Validators.maxLength(120)]],
    });

    this.account$ = this.store.select(selectAccountDetail);

    this.accountLoading$ = this.store.select(selectAccountDetailLoading);

    this.accountError$ = this.store.select(selectAccountDetailError);

    this.transactions$ = this.store.select(selectTransactions);

    this.transactionsLoading$ = this.store.select(selectTransactionsLoading);

    this.transactionsError$ = this.store.select(selectTransactionsError);

    this.isLastPage$ = this.store.select(selectIsLastPage);

    this.totalElements$ = this.store.select(selectTotalElements);

    const { startDate, endDate } = this.getDefaultChartDateRange();

    this.chartFilterForm = this.fb.group({
      startDate: [startDate, Validators.required],

      endDate: [endDate, Validators.required],
    });
  }

  ngOnInit(): void {
    this.iban = this.route.snapshot.paramMap.get('iban')!;

    this.store.dispatch(loadAccountDetail({ iban: this.iban }));

    this.store.dispatch(loadTransactions({ iban: this.iban, page: 0, size: PAGE_SIZE }));

    this.loadCurrencies();

    const { startDate, endDate } = this.chartFilterForm.value;

    this.loadBalanceChart(this.iban, `${startDate}T00:00:00`, `${endDate}T23:59:59`);

    this.account$

      .pipe(
        filter((account): account is Account => !!account?.currency),

        takeUntil(this.destroy$),
      )

      .subscribe((account) => {
        this.accountCurrency = account.currency;

        this.operationForm.patchValue({ currency: account.currency });
      });
  }

  applyChartFilter(): void {
    this.chartFilterForm.updateValueAndValidity();

    if (this.chartFilterForm.invalid) {
      this.chartFilterForm.markAllAsTouched();

      return;
    }

    const startDate = this.chartFilterForm.get('startDate')?.value;

    const endDate = this.chartFilterForm.get('endDate')?.value;

    this.loadBalanceChart(this.iban, `${startDate}T00:00:00`, `${endDate}T23:59:59`);
  }

  private resetBalanceChartVisualState(): void {
    this.balanceChartPath = null;

    this.balanceChartPoints = [];

    this.balanceChartXTicks = [];

    this.balanceChartYTicks = [];

    this.hoveredChartPoint = null;
  }

  private loadBalanceChart(iban: string | number, startDate: string, endDate: string): void {
    const requestId = ++this.balanceChartRequestId;

    this.balanceChartLoading = true;

    this.balanceChartError = null;

    this.resetBalanceChartVisualState();

    this.cdr.detectChanges();

    this.transactionsService

      .getBalanceChart(String(iban), startDate, endDate)

      .pipe(
        finalize(() => {
          if (requestId !== this.balanceChartRequestId) {
            return;
          }

          this.balanceChartLoading = false;

          this.cdr.detectChanges();
        }),
      )

      .subscribe({
        next: (data) => {
          if (requestId !== this.balanceChartRequestId) {
            return;
          }

          this.balanceChartData = (data || [])

            .slice()

            .sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());

          this.buildChartFromData();

          this.cdr.detectChanges();
        },

        error: (err) => {
          if (requestId !== this.balanceChartRequestId) {
            return;
          }

          // eslint-disable-next-line no-console

          console.error('[AccountOverview] balance chart error:', err);

          this.balanceChartError = getApiErrorMessage(err, 'Unable to load balance chart.');

          this.showToast(this.balanceChartError, 'error');

          this.cdr.detectChanges();
        },
      });
  }

  private buildChartFromData(): void {
    const data = this.balanceChartData;

    if (!data || data.length === 0) {
      this.resetBalanceChartVisualState();

      return;
    }

    const w = this.balanceChartWidth;

    const h = this.balanceChartHeight;

    const plotLeft = this.balanceChartPaddingLeft;

    const plotRight = w - this.balanceChartPaddingRight;

    const plotTop = this.balanceChartPaddingTop;

    const plotBottom = h - this.balanceChartPaddingBottom;

    const plotWidth = plotRight - plotLeft;

    const plotHeight = plotBottom - plotTop;

    const times = data.map((d) => new Date(d.timestamp).getTime());

    const balances = data.map((d) => Number(d.balance));

    const tMin = Math.min(...times);

    const tMax = Math.max(...times);

    const bMin = Math.min(...balances);

    const bMax = Math.max(...balances);

    const xFor = (t: number) =>
      tMax === tMin
        ? plotLeft + plotWidth / 2
        : plotLeft + ((t - tMin) / (tMax - tMin)) * plotWidth;

    const yFor = (b: number) =>
      bMax === bMin
        ? plotTop + plotHeight / 2
        : plotTop + (1 - (b - bMin) / (bMax - bMin)) * plotHeight;

    const points = data.map((d) => {
      const balance = Number(d.balance);

      return {
        x: xFor(new Date(d.timestamp).getTime()),
        y: yFor(balance),
        label: this.formatDate(d.timestamp),
        balance,
        balanceLabel: this.formatChartBalance(balance),
      };
    });

    this.balanceChartPoints = points;

    const tickCount = 5;

    this.balanceChartXTicks = this.buildLinearTicks(tMin, tMax, tickCount).map((value) => ({
      x: xFor(value),
      label: this.formatChartAxisDate(value),
    }));

    this.balanceChartYTicks = this.buildLinearTicks(bMin, bMax, tickCount).map((value) => ({
      y: yFor(value),
      label: this.formatChartBalance(value),
    }));

    const d = points

      .map((pnt, i) => `${i === 0 ? 'M' : 'L'} ${pnt.x.toFixed(2)} ${pnt.y.toFixed(2)}`)

      .join(' ');

    this.balanceChartPath = d;
  }

  onChartPointHover(point: AccountOverviewComponent['balanceChartPoints'][number]): void {
    if (this.hoveredChartPoint === point) {
      return;
    }

    this.hoveredChartPoint = point;
  }

  onChartPointLeave(): void {
    if (!this.hoveredChartPoint) {
      return;
    }

    this.hoveredChartPoint = null;
  }

  getChartTooltipLeft(point: { x: number }): number {
    return (point.x / this.balanceChartWidth) * 100;
  }

  getChartTooltipTop(point: { y: number }): number {
    return (point.y / this.balanceChartHeight) * 100;
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

    this.destroy$.next();

    this.destroy$.complete();

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

      currency: rawValue.currency,
    };

    this.operationLoading = true;

    this.operationError = null;

    this.operationSuccess = null;

    this.cdr.detectChanges();

    const operation$ =
      rawValue.type === 'DEBIT'
        ? this.transactionsService.debitAccount(payload)
        : this.transactionsService.creditAccount(payload);

    operation$.pipe(timeout(15000)).subscribe({
      next: () => {
        this.ngZone.run(() => {
          this.operationLoading = false;

          this.operationSuccess =
            rawValue.type === 'DEBIT'
              ? 'Debit completed successfully.'
              : 'Credit completed successfully.';

          this.showToast(this.operationSuccess, 'success');

          this.store.dispatch(loadAccountDetail({ iban: this.iban }));

          this.store.dispatch(clearTransactions());

          this.store.dispatch(loadTransactions({ iban: this.iban, page: 0, size: PAGE_SIZE }));

          const { startDate, endDate } = this.chartFilterForm.value;

          this.loadBalanceChart(this.iban, `${startDate}T00:00:00`, `${endDate}T23:59:59`);
        });
      },

      error: (error) => {
        this.ngZone.run(() => {
          this.operationLoading = false;

          this.operationError = this.getOperationErrorMessage(error, rawValue.type);

          this.showToast(this.operationError, 'error');
        });
      },
    });
  }

  setOperationType(type: TransactionOperationType): void {
    this.operationForm.patchValue({
      type,

      currency: this.accountCurrency ?? this.operationForm.get('currency')?.value,
    });

    this.operationError = null;

    this.operationSuccess = null;
  }

  private loadCurrencies(): void {
    this.currenciesLoading = true;

    this.currenciesError = null;

    this.currenciesService

      .getCurrencies()

      .pipe(finalize(() => (this.currenciesLoading = false)))

      .subscribe({
        next: (currencies) => {
          this.currencies = currencies;
        },

        error: (error) => {
          this.currenciesError = getApiErrorMessage(
            error,

            'Unable to load currencies. Please try again.',
          );

          this.showToast(this.currenciesError, 'error');
        },
      });
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

  hasCurrencyConversion(sourceCurrency: string, targetCurrency: string): boolean {
    return sourceCurrency?.toLowerCase() !== targetCurrency?.toLowerCase();
  }

  private getDefaultChartDateRange(): { startDate: string; endDate: string } {
    const endDate = new Date();
    const startDate = new Date(endDate);
    startDate.setFullYear(startDate.getFullYear() - 1);

    return {
      startDate: this.toDateInputValue(startDate),
      endDate: this.toDateInputValue(endDate),
    };
  }

  private toDateInputValue(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private buildLinearTicks(min: number, max: number, count: number): number[] {
    if (count <= 1 || min === max) {
      return [min];
    }

    const step = (max - min) / (count - 1);

    return Array.from({ length: count }, (_, index) => min + step * index);
  }

  private formatChartBalance(value: number): string {
    if (value > 0 && value < 0.01) {
      return value.toString();
    }

    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(value);
  }

  private formatChartAxisDate(timestamp: number): string {
    return new Date(timestamp).toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: 'short',
    });
  }
}
