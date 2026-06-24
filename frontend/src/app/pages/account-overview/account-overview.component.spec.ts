import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute, provideRouter, Router } from '@angular/router';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { AccountOverviewComponent } from './account-overview.component';
import { TransactionsService } from '../../services/transactions.service';
import { CurrenciesService } from '../../services/currencies.service';
import {
  clearAccountDetail,
  loadAccountDetail,
} from '../../store/account-detail/account-detail.actions';
import {
  clearTransactions,
  loadTransactions,
} from '../../store/transactions/transactions.actions';
import { selectAccountDetail } from '../../store/account-detail/account-detail.selectors';
import { Account } from '../../store/accounts/accounts.actions';
import { SmartCurrencyPipe } from '../../pipes/smart-currency-pipe';

const TEST_IBAN = 'PT50000201231234567890154';

const mockAccount: Account = {
  uuid: 'account-uuid',
  iban: TEST_IBAN,
  currency: 'EUR',
  balance: 1500.5,
  user: {
    uuid: 'user-uuid',
    username: 'john',
    personalId: '123456789',
    email: 'john@example.com',
  },
};

class IntersectionObserverMock {
  observe = jest.fn();

  disconnect = jest.fn();

  unobserve = jest.fn();

  constructor(public callback: IntersectionObserverCallback) {}
}

@Component({
  selector: 'app-sidebar',
  template: '',
  standalone: true,
})
class SidebarStubComponent {}

describe('AccountOverviewComponent', () => {
  let component: AccountOverviewComponent;
  let fixture: ComponentFixture<AccountOverviewComponent>;
  let store: MockStore;
  let dispatchSpy: jest.SpyInstance;
  let router: Router;
  let navigateSpy: jest.SpyInstance;
  let transactionsService: {
    getBalanceChart: jest.Mock;
    creditAccount: jest.Mock;
    debitAccount: jest.Mock;
  };
  let currenciesService: { getCurrencies: jest.Mock };

  beforeEach(async () => {
    (global as unknown as { IntersectionObserver: typeof IntersectionObserver }).IntersectionObserver =
      IntersectionObserverMock as unknown as typeof IntersectionObserver;

    transactionsService = {
      getBalanceChart: jest.fn().mockReturnValue(of([])),
      creditAccount: jest.fn().mockReturnValue(of({ uuid: 'tx-1' })),
      debitAccount: jest.fn().mockReturnValue(of({ uuid: 'tx-2' })),
    };
    currenciesService = {
      getCurrencies: jest.fn().mockReturnValue(of([{ code: 'EUR', description: 'Euro' }])),
    };

    await TestBed.configureTestingModule({
      imports: [AccountOverviewComponent],
      providers: [
        provideRouter([]),
        provideMockStore({
          selectors: [{ selector: selectAccountDetail, value: null }],
        }),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'iban' ? TEST_IBAN : null),
              },
            },
          },
        },
        { provide: TransactionsService, useValue: transactionsService },
        { provide: CurrenciesService, useValue: currenciesService },
      ],
    })
      .overrideComponent(AccountOverviewComponent, {
        set: {
          imports: [CommonModule, ReactiveFormsModule, SmartCurrencyPipe, SidebarStubComponent],
        },
      })
      .compileComponents();

    store = TestBed.inject(MockStore);
    router = TestBed.inject(Router);
    navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true);
    dispatchSpy = jest.spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(AccountOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should dispatch account and transaction loads for the route iban', () => {
      expect(dispatchSpy).toHaveBeenCalledWith(loadAccountDetail({ iban: TEST_IBAN }));
      expect(dispatchSpy).toHaveBeenCalledWith(
        loadTransactions({ iban: TEST_IBAN, page: 0, size: 5 }),
      );
    });

    it('should load currencies and balance chart', () => {
      expect(currenciesService.getCurrencies).toHaveBeenCalled();
      expect(transactionsService.getBalanceChart).toHaveBeenCalledWith(
        TEST_IBAN,
        expect.stringMatching(/T00:00:00$/),
        expect.stringMatching(/T23:59:59$/),
      );
    });

    it('should patch operation currency when account is available', () => {
      store.overrideSelector(selectAccountDetail, mockAccount);
      store.refreshState();
      fixture.detectChanges();

      expect(component.operationForm.get('currency')?.value).toBe('EUR');
    });
  });

  describe('ngOnDestroy', () => {
    it('should clear store state and complete subscriptions', () => {
      component.ngAfterViewInit();
      component.ngOnDestroy();

      expect(dispatchSpy).toHaveBeenCalledWith(clearAccountDetail());
      expect(dispatchSpy).toHaveBeenCalledWith(clearTransactions());
    });
  });

  describe('navigation', () => {
    it('goBack should navigate to dashboard', () => {
      component.goBack();

      expect(navigateSpy).toHaveBeenCalledWith(['/dashboard']);
    });

    it('goToTransaction should navigate to transaction detail', () => {
      component.goToTransaction('tx-uuid');

      expect(navigateSpy).toHaveBeenCalledWith(['/transactions', 'tx-uuid']);
    });
  });

  describe('formatting helpers', () => {
    it('formatIban should group iban in blocks of four characters', () => {
      expect(component.formatIban('PT50000201231234567890154')).toBe(
        'PT50 0002 0123 1234 5678 9015 4',
      );
    });

    it('formatDate should format timestamp in pt-BR locale', () => {
      const formatted = component.formatDate('2024-06-15T14:30:00');

      expect(formatted).toContain('2024');
      expect(formatted).toContain('jun');
    });

    it('hasCurrencyConversion should compare currencies case-insensitively', () => {
      expect(component.hasCurrencyConversion('EUR', 'USD')).toBe(true);
      expect(component.hasCurrencyConversion('eur', 'EUR')).toBe(false);
    });
  });

  describe('setOperationType', () => {
    it('should update operation type and clear feedback messages', () => {
      component.operationError = 'previous error';
      component.operationSuccess = 'previous success';
      store.overrideSelector(selectAccountDetail, mockAccount);
      store.refreshState();
      fixture.detectChanges();

      component.setOperationType('DEBIT');

      expect(component.operationForm.get('type')?.value).toBe('DEBIT');
      expect(component.operationForm.get('currency')?.value).toBe('EUR');
      expect(component.operationError).toBeNull();
      expect(component.operationSuccess).toBeNull();
    });
  });

  describe('applyChartFilter', () => {
    it('should mark form as touched when filter is invalid', () => {
      const markAllAsTouchedSpy = jest.spyOn(component.chartFilterForm, 'markAllAsTouched');

      component.chartFilterForm.patchValue({ startDate: '', endDate: '' });
      component.applyChartFilter();

      expect(markAllAsTouchedSpy).toHaveBeenCalled();
      expect(transactionsService.getBalanceChart).toHaveBeenCalledTimes(1);
    });

    it('should reload balance chart when filter is valid', () => {
      component.chartFilterForm.patchValue({
        startDate: '2024-01-01',
        endDate: '2024-06-01',
      });

      component.applyChartFilter();

      expect(transactionsService.getBalanceChart).toHaveBeenCalledWith(
        TEST_IBAN,
        '2024-01-01T00:00:00',
        '2024-06-01T23:59:59',
      );
    });
  });

  describe('balance chart', () => {
    it('should build chart geometry when data is returned', fakeAsync(() => {
      transactionsService.getBalanceChart.mockReturnValue(
        of([
          { timestamp: '2024-01-02T00:00:00', balance: 200 },
          { timestamp: '2024-01-01T00:00:00', balance: 100 },
        ]),
      );

      component.applyChartFilter();
      tick();

      expect(component.balanceChartLoading).toBe(false);
      expect(component.balanceChartPoints.length).toBe(2);
      expect(component.balanceChartPath).toContain('M');
      expect(component.balanceChartXTicks.length).toBeGreaterThan(0);
      expect(component.balanceChartYTicks.length).toBeGreaterThan(0);
    }));

    it('should set error and toast when chart request fails', fakeAsync(() => {
      transactionsService.getBalanceChart.mockReturnValue(
        throwError(() => ({ error: { message: 'Chart unavailable' } })),
      );

      component.applyChartFilter();
      tick();

      expect(component.balanceChartError).toBe('Chart unavailable');
      expect(component.toastMessage).toBe('Chart unavailable');
      expect(component.toastType).toBe('error');
    }));

    it('should reset visual state when chart data is empty', fakeAsync(() => {
      transactionsService.getBalanceChart.mockReturnValue(of([]));

      component.applyChartFilter();
      tick();

      expect(component.balanceChartPoints).toEqual([]);
      expect(component.balanceChartPath).toBeNull();
    }));
  });

  describe('chart interactions', () => {
    const point = { x: 120, y: 80, label: 'Jan', balance: 500, balanceLabel: '500.00' };

    it('onChartPointHover should store hovered point', () => {
      component.onChartPointHover(point);

      expect(component.hoveredChartPoint).toBe(point);
    });

    it('onChartPointHover should ignore duplicate point', () => {
      component.hoveredChartPoint = point;

      component.onChartPointHover(point);

      expect(component.hoveredChartPoint).toBe(point);
    });

    it('onChartPointLeave should clear hovered point', () => {
      component.hoveredChartPoint = point;

      component.onChartPointLeave();

      expect(component.hoveredChartPoint).toBeNull();
    });

    it('getChartTooltipLeft should return percentage based on chart width', () => {
      expect(component.getChartTooltipLeft({ x: 360 })).toBe(50);
    });

    it('getChartTooltipTop should return percentage based on chart height', () => {
      expect(component.getChartTooltipTop({ y: 160 })).toBe(50);
    });
  });

  describe('submitOperation', () => {
    beforeEach(() => {
      component.operationForm.patchValue({
        type: 'CREDIT',
        amount: 25.5,
        currency: 'EUR',
        description: 'Test deposit',
      });
    });

    it('should not submit when form is invalid', () => {
      component.operationForm.patchValue({ amount: null });

      component.submitOperation();

      expect(transactionsService.creditAccount).not.toHaveBeenCalled();
      expect(transactionsService.debitAccount).not.toHaveBeenCalled();
      expect(component.operationForm.get('amount')?.touched).toBe(true);
    });

    it('should credit account and refresh data on success', fakeAsync(() => {
      component.submitOperation();
      tick();

      expect(transactionsService.creditAccount).toHaveBeenCalledWith({
        iban: TEST_IBAN,
        amount: 25.5,
        description: 'Test deposit',
        currency: 'EUR',
      });
      expect(component.operationLoading).toBe(false);
      expect(component.operationSuccess).toBe('Credit completed successfully.');
      expect(component.toastMessage).toBe('Credit completed successfully.');
      expect(dispatchSpy).toHaveBeenCalledWith(loadAccountDetail({ iban: TEST_IBAN }));
      expect(dispatchSpy).toHaveBeenCalledWith(clearTransactions());
      expect(dispatchSpy).toHaveBeenCalledWith(
        loadTransactions({ iban: TEST_IBAN, page: 0, size: 5 }),
      );
    }));

    it('should debit account when operation type is DEBIT', fakeAsync(() => {
      component.operationForm.patchValue({ type: 'DEBIT' });

      component.submitOperation();
      tick();

      expect(transactionsService.debitAccount).toHaveBeenCalledWith({
        iban: TEST_IBAN,
        amount: 25.5,
        description: 'Test deposit',
        currency: 'EUR',
      });
      expect(component.operationSuccess).toBe('Debit completed successfully.');
    }));

    it('should show operation error on failure', fakeAsync(() => {
      transactionsService.creditAccount.mockReturnValue(
        throwError(() => ({ error: { message: 'Insufficient funds' } })),
      );

      component.submitOperation();
      tick();

      expect(component.operationLoading).toBe(false);
      expect(component.operationError).toBe('Insufficient funds');
      expect(component.toastMessage).toBe('Insufficient funds');
      expect(component.toastType).toBe('error');
    }));
  });

  describe('loadCurrencies', () => {
    it('should populate currencies on success', fakeAsync(() => {
      tick();

      expect(component.currencies).toEqual([{ code: 'EUR', description: 'Euro' }]);
      expect(component.currenciesLoading).toBe(false);
    }));

    it('should set error and toast when currencies request fails', fakeAsync(() => {
      currenciesService.getCurrencies.mockReturnValue(
        throwError(() => ({ error: { message: 'Currency service down' } })),
      );

      fixture = TestBed.createComponent(AccountOverviewComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
      tick();

      expect(component.currenciesError).toBe('Currency service down');
      expect(component.toastMessage).toBe('Currency service down');
      expect(component.toastType).toBe('error');
    }));
  });

  describe('ngAfterViewInit', () => {
    it('should observe scroll sentinel when available', () => {
      const observeMock = jest.fn();
      const observerInstance = {
        observe: observeMock,
        disconnect: jest.fn(),
      };

      (global as unknown as { IntersectionObserver: typeof IntersectionObserver }).IntersectionObserver =
        jest.fn().mockImplementation(() => observerInstance) as unknown as typeof IntersectionObserver;

      component.scrollSentinel = {
        nativeElement: document.createElement('div'),
      } as AccountOverviewComponent['scrollSentinel'];

      component.ngAfterViewInit();

      expect(observeMock).toHaveBeenCalledWith(component.scrollSentinel.nativeElement);
    });
  });
});
