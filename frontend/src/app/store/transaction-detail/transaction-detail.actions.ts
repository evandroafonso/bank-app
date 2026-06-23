import { createAction, props } from '@ngrx/store';
import { Transaction } from '../transactions/transactions.actions';

export const loadTransactionDetail = createAction(
  '[Transaction Detail] Load Transaction Detail',
  props<{ uuid: string }>(),
);

export const loadTransactionDetailSuccess = createAction(
  '[Transaction Detail] Load Transaction Detail Success',
  props<{ transaction: Transaction }>(),
);

export const loadTransactionDetailFailure = createAction(
  '[Transaction Detail] Load Transaction Detail Failure',
  props<{ error: string }>(),
);

export const clearTransactionDetail = createAction('[Transaction Detail] Clear');
