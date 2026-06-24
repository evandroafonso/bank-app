import * as fromActions from './transaction-detail.actions';
import { Transaction } from '../transactions/transactions.actions';

describe('Transaction Detail Actions', () => {
  it('should create loadTransactionDetail action', () => {
    const payload = { uuid: '123e4567-e89b-12d3-a456-426614174000' };
    const action = fromActions.loadTransactionDetail(payload);

    expect(action.type).toBe('[Transaction Detail] Load Transaction Detail');
    expect(action.uuid).toBe(payload.uuid);
  });

  it('should create loadTransactionDetailSuccess action', () => {
    const mockTransaction = {} as Transaction;
    const action = fromActions.loadTransactionDetailSuccess({ transaction: mockTransaction });

    expect(action.type).toBe('[Transaction Detail] Load Transaction Detail Success');
    expect(action.transaction).toEqual(mockTransaction);
  });

  it('should create loadTransactionDetailFailure action', () => {
    const payload = { error: 'Failed to load transaction details' };
    const action = fromActions.loadTransactionDetailFailure(payload);

    expect(action.type).toBe('[Transaction Detail] Load Transaction Detail Failure');
    expect(action.error).toBe(payload.error);
  });

  it('should create clearTransactionDetail action', () => {
    const action = fromActions.clearTransactionDetail();

    expect(action.type).toBe('[Transaction Detail] Clear');
  });
});
