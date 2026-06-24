import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

import { DashboardComponent } from './dashboard.component';
import { AccountsService } from '../../services/accounts.service';
import { CurrenciesService } from '../../services/currencies.service';
import { loadAccounts } from '../../store/accounts/accounts.actions';
import {
  selectAccounts,
  selectAccountsLoading,
  selectAccountsError,
  selectTotalBalance,
} from '../../store/accounts/accounts.selectors';

@Component({
  selector: 'app-sidebar',
  template: '',
  standalone: true,
})
class SidebarStubComponent {}

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let store: MockStore;
  let dispatchSpy: jest.SpyInstance;
  let router: Router;
  let navigateSpy: jest.SpyInstance;

  let accountsService: {
    createAccount: jest.Mock;
  };

  let currenciesService: {
    getCurrencies: jest.Mock;
  };

  beforeEach(async () => {
    accountsService = {
      createAccount: jest.fn(),
    };

    currenciesService = {
      getCurrencies: jest.fn().mockReturnValue(
        of([{ code: 'EUR', description: 'Euro' }]),
      ),
    };

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([]),
        provideMockStore({
          selectors: [
            { selector: selectAccounts, value: [] },
            { selector: selectAccountsLoading, value: false },
            { selector: selectAccountsError, value: null },
            { selector: selectTotalBalance, value: 1000 },
          ],
        }),
        {
          provide: AccountsService,
          useValue: accountsService,
        },
        {
          provide: CurrenciesService,
          useValue: currenciesService,
        },
      ],
    })
      .overrideComponent(DashboardComponent, {
        set: {
          imports: [CommonModule, ReactiveFormsModule, SidebarStubComponent],
        },
      })
      .compileComponents();

    store = TestBed.inject(MockStore);
    router = TestBed.inject(Router);

    dispatchSpy = jest.spyOn(store, 'dispatch');
    navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture = TestBed.createComponent(DashboardComponent);
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
    it('should dispatch loadAccounts', () => {
      expect(dispatchSpy).toHaveBeenCalledWith(loadAccounts());
    });

    it('should load currencies', () => {
      expect(currenciesService.getCurrencies).toHaveBeenCalled();
      expect(component.currencies).toEqual([
        { code: 'EUR', description: 'Euro' },
      ]);
    });
  });

  describe('goToAccount', () => {
    it('should navigate to account detail', () => {
      component.goToAccount('PT50000201231234567890154');

      expect(navigateSpy).toHaveBeenCalledWith([
        '/accounts',
        'PT50000201231234567890154',
      ]);
    });
  });

  describe('formatIban', () => {
    it('should format iban in groups of four characters', () => {
      expect(
        component.formatIban('PT50000201231234567890154'),
      ).toBe('PT50 0002 0123 1234 5678 9015 4');
    });
  });

  describe('openCreateAccountModal', () => {
    it('should open modal and clear messages', () => {
      component.createAccountError = 'error';
      component.createAccountSuccess = 'success';

      component.openCreateAccountModal();

      expect(component.isCreateAccountModalOpen).toBe(true);
      expect(component.createAccountError).toBeNull();
      expect(component.createAccountSuccess).toBeNull();
    });

    it('should reload currencies when list is empty', () => {
      component.currencies = [];

      component.openCreateAccountModal();

      expect(currenciesService.getCurrencies).toHaveBeenCalledTimes(2);
    });

    it('should not reload currencies when already loaded', () => {
      component.currencies = [
        { code: 'EUR', description: 'Euro' },
      ];

      currenciesService.getCurrencies.mockClear();

      component.openCreateAccountModal();

      expect(currenciesService.getCurrencies).not.toHaveBeenCalled();
    });
  });

  describe('closeCreateAccountModal', () => {
    it('should close modal and reset form', () => {
      component.isCreateAccountModalOpen = true;
      component.createAccountForm.patchValue({
        currency: 'EUR',
      });

      component.createAccountError = 'error';
      component.createAccountSuccess = 'success';

      component.closeCreateAccountModal();

      expect(component.isCreateAccountModalOpen).toBe(false);
      expect(component.createAccountForm.get('currency')?.value).toBeNull();
      expect(component.createAccountError).toBeNull();
      expect(component.createAccountSuccess).toBeNull();
    });

    it('should do nothing when create account is loading', () => {
      component.isCreateAccountModalOpen = true;
      component.createAccountLoading = true;

      component.closeCreateAccountModal();

      expect(component.isCreateAccountModalOpen).toBe(true);
    });
  });

  describe('createAccount', () => {
    beforeEach(() => {
      component.createAccountForm.patchValue({
        currency: 'EUR',
      });
    });

    it('should not submit invalid form', () => {
      component.createAccountForm.patchValue({
        currency: '',
      });

      const touchSpy = jest.spyOn(
        component.createAccountForm,
        'markAllAsTouched',
      );

      component.createAccount();

      expect(touchSpy).toHaveBeenCalled();
      expect(accountsService.createAccount).not.toHaveBeenCalled();
    });

    it('should not submit when already loading', () => {
      component.createAccountLoading = true;

      component.createAccount();

      expect(accountsService.createAccount).not.toHaveBeenCalled();
    });

    it('should create account successfully', fakeAsync(() => {
      accountsService.createAccount.mockReturnValue(
        of({
          iban: 'PT50000201231234567890154',
        }),
      );

      component.isCreateAccountModalOpen = true;

      component.createAccount();
      tick();

      expect(accountsService.createAccount).toHaveBeenCalledWith('EUR');

      expect(component.createAccountLoading).toBe(false);

      expect(component.createAccountSuccess).toBe(
        'Account PT50 0002 0123 1234 5678 9015 4 created successfully.',
      );

      expect(component.isCreateAccountModalOpen).toBe(false);

      expect(dispatchSpy).toHaveBeenCalledWith(loadAccounts());
    }));

    it('should handle create account error', fakeAsync(() => {
      accountsService.createAccount.mockReturnValue(
        throwError(() => ({
          error: {
            message: 'Currency not supported',
          },
        })),
      );

      component.createAccount();
      tick();

      expect(component.createAccountLoading).toBe(false);
      expect(component.createAccountError).toBe(
        'Currency not supported',
      );
    }));
  });

  describe('loadCurrencies', () => {
    it('should populate currencies on success', fakeAsync(() => {
      tick();

      expect(component.currencies).toEqual([
        {
          code: 'EUR',
          description: 'Euro',
        },
      ]);

      expect(component.currenciesLoading).toBe(false);
    }));

    it('should set error when currencies request fails', fakeAsync(() => {
      currenciesService.getCurrencies.mockReturnValue(
        throwError(() => ({
          error: {
            message: 'Currencies unavailable',
          },
        })),
      );

      fixture = TestBed.createComponent(DashboardComponent);
      component = fixture.componentInstance;

      fixture.detectChanges();
      tick();

      expect(component.currenciesLoading).toBe(false);

      expect(component.createAccountError).toBe(
        'Currencies unavailable',
      );
    }));
  });
});