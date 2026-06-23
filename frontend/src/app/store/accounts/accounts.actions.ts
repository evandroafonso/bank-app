import { createAction, props } from '@ngrx/store';

export interface AccountUser {
  uuid: string;
  username: string;
  personalId: string;
  email: string;
}

export interface Account {
  uuid: string;
  iban: string;
  currency: string;
  balance: number;
  user: AccountUser;
}

export const loadAccounts = createAction('[Accounts] Load Accounts');

export const loadAccountsSuccess = createAction(
  '[Accounts] Load Accounts Success',
  props<{ accounts: Account[] }>(),
);

export const loadAccountsFailure = createAction(
  '[Accounts] Load Accounts Failure',
  props<{ error: string }>(),
);
