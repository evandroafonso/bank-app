import { createFeatureSelector, createSelector } from '@ngrx/store';
import { TransactionDetailState } from './transaction-detail.reducer';

export const selectTransactionDetailState =
  createFeatureSelector<TransactionDetailState>('transactionDetail');

export const selectTransactionDetail = createSelector(
  selectTransactionDetailState,
  (state) => state.transaction,
);

export const selectTransactionDetailLoading = createSelector(
  selectTransactionDetailState,
  (state) => state.loading,
);

export const selectTransactionDetailError = createSelector(
  selectTransactionDetailState,
  (state) => state.error,
);
