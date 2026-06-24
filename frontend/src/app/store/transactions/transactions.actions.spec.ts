import * as fromActions from './transactions.actions';
import { TransactionPage } from './transactions.actions';

describe('Transactions Actions', () => {
  it('should create loadTransactions action', () => {
    const payload = { iban: 'BR1234567890', page: 1, size: 20 };
    const action = fromActions.loadTransactions(payload);

    expect(action.type).toBe('[Transactions] Load Transactions');
    expect(action.iban).toBe(payload.iban);
    expect(action.page).toBe(payload.page);
    expect(action.size).toBe(payload.size);
  });

  it('should create loadTransactionsSuccess action', () => {
    const mockData: TransactionPage = {
      content: [],
      last: true,
      totalElements: 0,
      totalPages: 1,
      number: 0,
    };
    const action = fromActions.loadTransactionsSuccess({ data: mockData });

    expect(action.type).toBe('[Transactions] Load Transactions Success');
    expect(action.data).toEqual(mockData);
  });

  it('should create loadTransactionsFailure action', () => {
    const payload = { error: 'Internal Server Error' };
    const action = fromActions.loadTransactionsFailure(payload);

    expect(action.type).toBe('[Transactions] Load Transactions Failure');
    expect(action.error).toBe(payload.error);
  });

  it('should create clearTransactions action', () => {
    const action = fromActions.clearTransactions();

    expect(action.type).toBe('[Transactions] Clear');
  });
});
