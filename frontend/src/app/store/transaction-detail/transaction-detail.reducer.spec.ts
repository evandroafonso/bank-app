import { transactionDetailReducer, initialState } from './transaction-detail.reducer';
import * as fromActions from './transaction-detail.actions';
import { Transaction } from '../transactions/transactions.actions';

describe('TransactionDetail Reducer', () => {
  it('should return the default state', () => {
    const action = { type: 'Unknown' };
    const state = transactionDetailReducer(undefined, action);

    expect(state).toBe(initialState);
  });

  it('should set loading to true and clear previous data on loadTransactionDetail', () => {
    const action = fromActions.loadTransactionDetail({ uuid: '123' });
    const state = transactionDetailReducer(initialState, action);

    expect(state).toEqual({
      transaction: null,
      loading: true,
      error: null,
    });
  });

  it('should set transaction and set loading to false on loadTransactionDetailSuccess', () => {
    const mockTransaction = {} as Transaction;
    const action = fromActions.loadTransactionDetailSuccess({ transaction: mockTransaction });
    const previousState = { ...initialState, loading: true };
    const state = transactionDetailReducer(previousState, action);

    expect(state).toEqual({
      transaction: mockTransaction,
      loading: false,
      error: null,
    });
  });

  it('should set error and set loading to false on loadTransactionDetailFailure', () => {
    const errorMsg = 'Error loading transaction';
    const action = fromActions.loadTransactionDetailFailure({ error: errorMsg });
    const previousState = { ...initialState, loading: true };
    const state = transactionDetailReducer(previousState, action);

    expect(state).toEqual({
      transaction: null,
      loading: false,
      error: errorMsg,
    });
  });

  it('should reset to initialState on clearTransactionDetail', () => {
    const modifiedState = {
      transaction: {} as Transaction,
      loading: false,
      error: 'some error',
    };
    const action = fromActions.clearTransactionDetail();
    const state = transactionDetailReducer(modifiedState, action);

    expect(state).toEqual(initialState);
  });
});
