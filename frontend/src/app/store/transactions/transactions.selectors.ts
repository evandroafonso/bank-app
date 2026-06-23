import { createFeatureSelector, createSelector } from '@ngrx/store';
import { TransactionsState } from './transactions.reducer';

export const selectTransactionsState = createFeatureSelector<TransactionsState>('transactions');

export const selectTransactions = createSelector(
  selectTransactionsState,
  (state) => state.transactions,
);

export const selectTransactionsLoading = createSelector(
  selectTransactionsState,
  (state) => state.loading,
);

export const selectTransactionsError = createSelector(
  selectTransactionsState,
  (state) => state.error,
);

export const selectIsLastPage = createSelector(selectTransactionsState, (state) => state.isLast);

export const selectNextPage = createSelector(
  selectTransactionsState,
  (state) => state.currentPage + 1,
);

export const selectTotalElements = createSelector(
  selectTransactionsState,
  (state) => state.totalElements,
);
