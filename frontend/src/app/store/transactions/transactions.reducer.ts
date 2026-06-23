import { createReducer, on } from '@ngrx/store';
import {
  Transaction,
  loadTransactions,
  loadTransactionsSuccess,
  loadTransactionsFailure,
  clearTransactions,
} from './transactions.actions';

export interface TransactionsState {
  transactions: Transaction[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  isLast: boolean;
  loading: boolean;
  error: string | null;
}

export const initialState: TransactionsState = {
  transactions: [],
  currentPage: -1,
  totalPages: 0,
  totalElements: 0,
  isLast: false,
  loading: false,
  error: null,
};

export const transactionsReducer = createReducer(
  initialState,

  on(loadTransactions, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(loadTransactionsSuccess, (state, { data }) => ({
    ...state,
    // Accumulate — append new page to existing list
    transactions: data.number === 0 ? data.content : [...state.transactions, ...data.content],
    currentPage: data.number,
    totalPages: data.totalPages,
    totalElements: data.totalElements,
    isLast: data.last,
    loading: false,
    error: null,
  })),

  on(loadTransactionsFailure, (state, { error }) => ({
    ...state,
    loading: false,
    error,
  })),

  on(clearTransactions, () => ({ ...initialState })),
);
