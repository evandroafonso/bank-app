import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable, of, throwError } from 'rxjs';
import { TransactionsEffects } from './transactions.effects';
import { TransactionsService } from '../../services/transactions.service';
import { TransactionPage } from './transactions.actions';
import * as fromActions from './transactions.actions';

describe('TransactionsEffects', () => {
  let actions$: Observable<any>;
  let effects: TransactionsEffects;
  let transactionsServiceMock: { getTransactionHistory: jest.Mock };

  beforeEach(() => {
    transactionsServiceMock = {
      getTransactionHistory: jest.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        TransactionsEffects,
        provideMockActions(() => actions$),
        { provide: TransactionsService, useValue: transactionsServiceMock },
      ],
    });

    effects = TestBed.inject(TransactionsEffects);
  });

  it('should return loadTransactionsSuccess on success', (done) => {
    const payload = { iban: 'BR123456', page: 0, size: 10 };
    const mockData = {
      content: [],
      last: true,
      totalElements: 0,
      totalPages: 1,
      number: 0,
    } as TransactionPage;

    actions$ = of(fromActions.loadTransactions(payload));
    transactionsServiceMock.getTransactionHistory.mockReturnValue(of(mockData));

    effects.loadTransactions$.subscribe((result) => {
      expect(result).toEqual(fromActions.loadTransactionsSuccess({ data: mockData }));
      expect(transactionsServiceMock.getTransactionHistory).toHaveBeenCalledWith(
        payload.iban,
        payload.page,
        payload.size,
      );
      done();
    });
  });

  it('should return loadTransactionsFailure on error', (done) => {
    const payload = { iban: 'BR123456', page: 0, size: 10 };
    const mockError = new Error('API Error');

    actions$ = of(fromActions.loadTransactions(payload));
    transactionsServiceMock.getTransactionHistory.mockReturnValue(throwError(() => mockError));

    effects.loadTransactions$.subscribe((result) => {
      const failureAction = result as ReturnType<typeof fromActions.loadTransactionsFailure>;

      expect(failureAction.type).toBe(fromActions.loadTransactionsFailure.type);
      expect(failureAction.error).toBeDefined();
      expect(transactionsServiceMock.getTransactionHistory).toHaveBeenCalledWith(
        payload.iban,
        payload.page,
        payload.size,
      );
      done();
    });
  });
});
