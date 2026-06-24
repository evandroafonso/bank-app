import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable, of, throwError } from 'rxjs';
import { TransactionDetailEffects } from './transaction-detail.effects';
import { TransactionsService } from '../../services/transactions.service';
import { Transaction } from '../transactions/transactions.actions';
import * as fromActions from './transaction-detail.actions';

describe('TransactionDetailEffects', () => {
  let actions$: Observable<any>;
  let effects: TransactionDetailEffects;
  let transactionsServiceMock: { getTransactionByUuid: jest.Mock };

  beforeEach(() => {
    transactionsServiceMock = {
      getTransactionByUuid: jest.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        TransactionDetailEffects,
        provideMockActions(() => actions$),
        { provide: TransactionsService, useValue: transactionsServiceMock },
      ],
    });

    effects = TestBed.inject(TransactionDetailEffects);
  });

  it('should return loadTransactionDetailSuccess on success', (done) => {
    const uuid = 'mock-uuid-123';
    const mockTransaction = {} as Transaction;

    actions$ = of(fromActions.loadTransactionDetail({ uuid }));
    transactionsServiceMock.getTransactionByUuid.mockReturnValue(of(mockTransaction));

    effects.loadTransactionDetail$.subscribe((result) => {
      expect(result).toEqual(
        fromActions.loadTransactionDetailSuccess({ transaction: mockTransaction }),
      );
      expect(transactionsServiceMock.getTransactionByUuid).toHaveBeenCalledWith(uuid);
      done();
    });
  });

  it('should return loadTransactionDetailFailure on error', (done) => {
    const uuid = 'mock-uuid-123';
    const mockError = new Error('API Error');

    actions$ = of(fromActions.loadTransactionDetail({ uuid }));
    transactionsServiceMock.getTransactionByUuid.mockReturnValue(throwError(() => mockError));

    effects.loadTransactionDetail$.subscribe((result) => {
      const failureAction = result as ReturnType<typeof fromActions.loadTransactionDetailFailure>;

      expect(failureAction.type).toBe(fromActions.loadTransactionDetailFailure.type);
      expect(failureAction.error).toBeDefined();
      expect(transactionsServiceMock.getTransactionByUuid).toHaveBeenCalledWith(uuid);
      done();
    });
  });
});
