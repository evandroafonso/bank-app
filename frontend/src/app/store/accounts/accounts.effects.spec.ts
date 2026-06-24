import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable, of, throwError } from 'rxjs';
import { AccountsEffects } from './accounts.effects';
import { AccountsService } from '../../services/accounts.service';
import { Account } from './accounts.actions';
import { loadAccounts, loadAccountsSuccess, loadAccountsFailure } from './accounts.actions';

describe('AccountsEffects', () => {
  let actions$: Observable<any>;
  let effects: AccountsEffects;
  let accountsServiceMock: jest.Mocked<AccountsService>;

  beforeEach(() => {
    const spy = {
      getAccounts: jest.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        AccountsEffects,
        provideMockActions(() => actions$),
        { provide: AccountsService, useValue: spy },
      ],
    });

    effects = TestBed.inject(AccountsEffects);
    accountsServiceMock = TestBed.inject(AccountsService) as jest.Mocked<AccountsService>;
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });

  it('should return loadAccountsSuccess on success', (done) => {
    const mockAccounts: Account[] = [
      {
        uuid: 'uuid-123',
        iban: 'EE1234567890',
        currency: 'EUR',
        balance: 1000,
        user: {} as any,
      },
    ];

    actions$ = of(loadAccounts());
    accountsServiceMock.getAccounts.mockReturnValue(of(mockAccounts));

    effects.loadAccounts$.subscribe((action) => {
      expect(action).toEqual(loadAccountsSuccess({ accounts: mockAccounts }));
      expect(accountsServiceMock.getAccounts).toHaveBeenCalled();
      done();
    });
  });

  it('should return loadAccountsFailure on failure', (done) => {
    const mockError = { message: 'Http failure response' };

    actions$ = of(loadAccounts());
    accountsServiceMock.getAccounts.mockReturnValue(throwError(() => mockError));

    effects.loadAccounts$.subscribe((action) => {
      const failureAction = action as ReturnType<typeof loadAccountsFailure>;

      expect(failureAction.type).toBe(loadAccountsFailure.type);
      expect(failureAction.error).toBe('Unable to load accounts. Please try again.');
      expect(accountsServiceMock.getAccounts).toHaveBeenCalled();
      done();
    });
  });
});
