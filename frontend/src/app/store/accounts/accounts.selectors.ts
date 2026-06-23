import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AccountsState } from './accounts.reducer';

export const selectAccountsState = createFeatureSelector<AccountsState>('accounts');

export const selectAccounts = createSelector(selectAccountsState, (state) => state.accounts);

export const selectAccountsLoading = createSelector(selectAccountsState, (state) => state.loading);

export const selectAccountsError = createSelector(selectAccountsState, (state) => state.error);

export const selectTotalBalance = createSelector(selectAccounts, (accounts) =>
  accounts.reduce((sum, acc) => sum + acc.balance, 0),
);
