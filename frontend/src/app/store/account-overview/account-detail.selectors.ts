import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AccountDetailState } from './account-detail.reducer';

export const selectAccountDetailState =
  createFeatureSelector<AccountDetailState>('accountDetail');

export const selectAccountDetail = createSelector(
  selectAccountDetailState,
  (state) => state.account,
);

export const selectAccountDetailLoading = createSelector(
  selectAccountDetailState,
  (state) => state.loading,
);

export const selectAccountDetailError = createSelector(
  selectAccountDetailState,
  (state) => state.error,
);
