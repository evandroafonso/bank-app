import { createAction, props } from '@ngrx/store';

export interface Transaction {
  uuid: string;
  sourceAmount: number;
  convertedAmount: number;
  exchangeRate: number;
  currency: string;
  targetCurrency: string;
  balance: number;
  type: 'CREDIT' | 'DEBIT';
  description: string;
  timestamp: string;
}

export interface TransactionPage {
  content: Transaction[];
  last: boolean;
  totalElements: number;
  totalPages: number;
  number: number;
}

export const loadTransactions = createAction(
  '[Transactions] Load Transactions',
  props<{ iban: string; page: number; size: number }>(),
);

export const loadTransactionsSuccess = createAction(
  '[Transactions] Load Transactions Success',
  props<{ data: TransactionPage }>(),
);

export const loadTransactionsFailure = createAction(
  '[Transactions] Load Transactions Failure',
  props<{ error: string }>(),
);

export const clearTransactions = createAction('[Transactions] Clear');
