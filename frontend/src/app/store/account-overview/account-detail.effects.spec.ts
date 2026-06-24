import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable, of, throwError } from 'rxjs';
import { AccountDetailEffects } from './account-detail.effects';
import { AccountsService } from '../../services/accounts.service';
import { Account } from '../accounts/accounts.actions';
import {
  loadAccountDetail,
  loadAccountDetailSuccess,
  loadAccountDetailFailure,
} from './account-detail.actions';

describe('AccountDetailEffects', () => {
  let actions$: Observable<any>;
  let effects: AccountDetailEffects;
  let accountsServiceMock: jest.Mocked<AccountsService>;

  beforeEach(() => {
    const spy = {
      getAccountByIban: jest.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        AccountDetailEffects,
        provideMockActions(() => actions$),
        { provide: AccountsService, useValue: spy },
      ],
    });

    effects = TestBed.inject(AccountDetailEffects);
    accountsServiceMock = TestBed.inject(AccountsService) as jest.Mocked<AccountsService>;
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });

  it('should return loadAccountDetailSuccess on success', (done) => {
    const iban = 'EE1234567890';
    const mockAccount: Account = {
      uuid: 'uuid-123',
      iban,
      currency: 'EUR',
      balance: 2500,
      user: {} as any,
    };

    actions$ = of(loadAccountDetail({ iban }));
    accountsServiceMock.getAccountByIban.mockReturnValue(of(mockAccount));

    effects.loadAccountDetail$.subscribe((action) => {
      expect(action).toEqual(loadAccountDetailSuccess({ account: mockAccount }));
      expect(accountsServiceMock.getAccountByIban).toHaveBeenCalledWith(iban);
      done();
    });
  });

  it('should return loadAccountDetailFailure on failure', (done) => {
    const iban = 'EE1234567890';
    const mockError = { message: 'Http failure response' };

    actions$ = of(loadAccountDetail({ iban }));
    accountsServiceMock.getAccountByIban.mockReturnValue(throwError(() => mockError));

    effects.loadAccountDetail$.subscribe((action) => {
      const failureAction = action as ReturnType<typeof loadAccountDetailFailure>;

      expect(failureAction.type).toBe(loadAccountDetailFailure.type);
      expect(failureAction.error).toBe('Unable to load account details.');
      expect(accountsServiceMock.getAccountByIban).toHaveBeenCalledWith(iban);
      done();
    });
  });
});
