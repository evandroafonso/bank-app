import * as fromSelectors from './transactions.selectors';
import { TransactionsState } from './transactions.reducer';
import { Transaction } from './transactions.actions';

describe('Transactions Selectors', () => {
  const mockTransactions = [{ uuid: 'tx-123' }, { uuid: 'tx-456' }] as Transaction[];

  const mockGlobalState = {
    transactions: {
      transactions: mockTransactions,
      currentPage: 2,
      totalPages: 5,
      totalElements: 45,
      isLast: false,
      loading: true,
      error: 'Timeout Error',
    } as TransactionsState,
  };

  it('should select the transactions state', () => {
    const result = fromSelectors.selectTransactionsState(mockGlobalState);
    expect(result).toEqual(mockGlobalState.transactions);
  });

  it('should select transactions list', () => {
    const result = fromSelectors.selectTransactions(mockGlobalState);
    expect(result).toEqual(mockTransactions);
  });

  it('should select loading state', () => {
    const result = fromSelectors.selectTransactionsLoading(mockGlobalState);
    expect(result).toBe(true);
  });

  it('should select error state', () => {
    const result = fromSelectors.selectTransactionsError(mockGlobalState);
    expect(result).toBe('Timeout Error');
  });

  it('should select isLastPage state', () => {
    const result = fromSelectors.selectIsLastPage(mockGlobalState);
    expect(result).toBe(false);
  });

  it('should select the next page number', () => {
    const result = fromSelectors.selectNextPage(mockGlobalState);
    expect(result).toBe(3);
  });

  it('should select total elements count', () => {
    const result = fromSelectors.selectTotalElements(mockGlobalState);
    expect(result).toBe(45);
  });
});
