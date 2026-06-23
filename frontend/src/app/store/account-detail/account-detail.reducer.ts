import { createReducer, on } from '@ngrx/store';
import { Account } from '../accounts/accounts.actions';
import {
  clearAccountDetail,
  loadAccountDetail,
  loadAccountDetailFailure,
  loadAccountDetailSuccess,
} from './account-detail.actions';

export interface AccountDetailState {
  account: Account | null;
  loading: boolean;
  error: string | null;
}

export const initialState: AccountDetailState = {
  account: null,
  loading: false,
  error: null,
};

export const accountDetailReducer = createReducer(
  initialState,

  on(loadAccountDetail, () => ({
    account: null,
    loading: true,
    error: null,
  })),

  on(loadAccountDetailSuccess, (state, { account }) => ({
    ...state,
    account,
    loading: false,
    error: null,
  })),

  on(loadAccountDetailFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  on(clearAccountDetail, () => ({ ...initialState })),
);
