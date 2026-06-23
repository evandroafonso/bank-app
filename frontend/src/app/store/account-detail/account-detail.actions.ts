import { createAction, props } from '@ngrx/store';
import { Account } from '../accounts/accounts.actions';

export const loadAccountDetail = createAction(
  '[Account Detail] Load Account Detail',
  props<{ iban: string }>(),
);

export const loadAccountDetailSuccess = createAction(
  '[Account Detail] Load Account Detail Success',
  props<{ account: Account }>(),
);

export const loadAccountDetailFailure = createAction(
  '[Account Detail] Load Account Detail Failure',
  props<{ error: string }>(),
);

export const clearAccountDetail = createAction('[Account Detail] Clear');
