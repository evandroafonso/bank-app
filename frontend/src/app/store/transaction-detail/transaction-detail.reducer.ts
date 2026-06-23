import { createReducer, on } from '@ngrx/store';
import { Transaction } from '../transactions/transactions.actions';
import {
  clearTransactionDetail,
  loadTransactionDetail,
  loadTransactionDetailFailure,
  loadTransactionDetailSuccess,
} from './transaction-detail.actions';

export interface TransactionDetailState {
  transaction: Transaction | null;
  loading: boolean;
  error: string | null;
}

export const initialState: TransactionDetailState = {
  transaction: null,
  loading: false,
  error: null,
};

export const transactionDetailReducer = createReducer(
  initialState,

  on(loadTransactionDetail, () => ({
    transaction: null,
    loading: true,
    error: null,
  })),

  on(loadTransactionDetailSuccess, (state, { transaction }) => ({
    ...state,
    transaction,
    loading: false,
    error: null,
  })),

  on(loadTransactionDetailFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  on(clearTransactionDetail, () => ({ ...initialState })),
);
