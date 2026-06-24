import { transactionsReducer, initialState, TransactionsState } from './transactions.reducer';
import * as fromActions from './transactions.actions';
import { Transaction, TransactionPage } from './transactions.actions';

describe('Transactions Reducer', () => {
  it('should return the default state', () => {
    const action = { type: 'Unknown' };
    const state = transactionsReducer(undefined, action);

    expect(state).toBe(initialState);
  });

  it('should set loading to true and clear error on loadTransactions', () => {
    const action = fromActions.loadTransactions({ iban: 'BR123', page: 0, size: 10 });
    const state = transactionsReducer(initialState, action);

    expect(state).toEqual({
      ...initialState,
      loading: true,
      error: null,
    });
  });

  it('should replace transactions if page number is 0 on loadTransactionsSuccess', () => {
    const previousState: TransactionsState = {
      ...initialState,
      transactions: [{ uuid: 'old-transaction' } as Transaction],
      loading: true,
    };

    const mockPage: TransactionPage = {
      content: [{ uuid: 'new-transaction-1' } as Transaction],
      number: 0,
      totalPages: 2,
      totalElements: 2,
      last: false,
    };

    const action = fromActions.loadTransactionsSuccess({ data: mockPage });
    const state = transactionsReducer(previousState, action);

    expect(state.transactions).toEqual(mockPage.content);
    expect(state.currentPage).toBe(0);
    expect(state.loading).toBe(false);
  });

  it('should accumulate transactions if page number is greater than 0 on loadTransactionsSuccess', () => {
    const previousState: TransactionsState = {
      ...initialState,
      transactions: [{ uuid: 'transaction-page-0' } as Transaction],
      loading: true,
    };

    const mockPage: TransactionPage = {
      content: [{ uuid: 'transaction-page-1' } as Transaction],
      number: 1,
      totalPages: 2,
      totalElements: 2,
      last: true,
    };

    const action = fromActions.loadTransactionsSuccess({ data: mockPage });
    const state = transactionsReducer(previousState, action);

    expect(state.transactions).toEqual([
      { uuid: 'transaction-page-0' },
      { uuid: 'transaction-page-1' },
    ]);
    expect(state.currentPage).toBe(1);
    expect(state.isLast).toBe(true);
    expect(state.loading).toBe(false);
  });

  it('should set error and set loading to false on loadTransactionsFailure', () => {
    const errorMsg = 'Failed to load transactions';
    const action = fromActions.loadTransactionsFailure({ error: errorMsg });
    const previousState = { ...initialState, loading: true };
    const state = transactionsReducer(previousState, action);

    expect(state).toEqual({
      ...initialState,
      loading: false,
      error: errorMsg,
    });
  });

  it('should reset to initialState on clearTransactions', () => {
    const modifiedState: TransactionsState = {
      transactions: [{ uuid: 'active-transaction' } as Transaction],
      currentPage: 2,
      totalPages: 5,
      totalElements: 50,
      isLast: false,
      loading: false,
      error: 'some error',
    };

    const action = fromActions.clearTransactions();
    const state = transactionsReducer(modifiedState, action);

    expect(state).toEqual(initialState);
  });
});
