import {
  Component,
  OnInit,
  OnDestroy,
  AfterViewInit,
  ElementRef,
  ViewChild,
  NgZone,
} from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { take } from 'rxjs/operators';
import { CommonModule } from '@angular/common';

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
import { logout } from '../../store/auth/auth.actions';
import { selectUser } from '../../store/auth/auth.selectors';
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

const PAGE_SIZE = 5;

@Component({
  selector: 'app-account-overview',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './account-overview.component.html',
  styleUrls: ['./account-overview.component.scss'],
})
export class AccountOverviewComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('scrollSentinel') scrollSentinel!: ElementRef;

  account$: Observable<Account | null>;
  accountLoading$: Observable<boolean>;
  accountError$: Observable<string | null>;
  user$: Observable<any>;
  transactions$: Observable<Transaction[]>;
  transactionsLoading$: Observable<boolean>;
  transactionsError$: Observable<string | null>;
  isLastPage$: Observable<boolean>;
  totalElements$: Observable<number>;

  private iban!: string;
  private observer!: IntersectionObserver;

  constructor(
    private store: Store,
    private route: ActivatedRoute,
    private router: Router,
    private ngZone: NgZone,
  ) {
    this.account$ = this.store.select(selectAccountDetail);
    this.accountLoading$ = this.store.select(selectAccountDetailLoading);
    this.accountError$ = this.store.select(selectAccountDetailError);
    this.user$ = this.store.select(selectUser);
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
    this.store.dispatch(clearAccountDetail());
    this.store.dispatch(clearTransactions());
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  goToTransaction(uuid: string): void {
    this.router.navigate(['/transactions', uuid]);
  }

  onLogout(): void {
    this.store.dispatch(logout());
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
