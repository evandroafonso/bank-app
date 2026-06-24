import * as fromSelectors from './transaction-detail.selectors';
import { TransactionDetailState } from './transaction-detail.reducer';
import { Transaction } from '../transactions/transactions.actions';

describe('TransactionDetail Selectors', () => {
  const mockTransaction = { uuid: '123' } as Transaction;

  const mockGlobalState = {
    transactionDetail: {
      transaction: mockTransaction,
      loading: true,
      error: 'Error message',
    } as TransactionDetailState,
  };

  it('should select the transaction detail state', () => {
    const result = fromSelectors.selectTransactionDetailState(mockGlobalState);
    expect(result).toEqual(mockGlobalState.transactionDetail);
  });

  it('should select the transaction', () => {
    const result = fromSelectors.selectTransactionDetail(mockGlobalState);
    expect(result).toEqual(mockTransaction);
  });

  it('should select the loading state', () => {
    const result = fromSelectors.selectTransactionDetailLoading(mockGlobalState);
    expect(result).toBe(true);
  });

  it('should select the error state', () => {
    const result = fromSelectors.selectTransactionDetailError(mockGlobalState);
    expect(result).toBe('Error message');
  });
});
