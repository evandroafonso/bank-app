import { accountsReducer, initialState, AccountsState } from './accounts.reducer';
import * as fromActions from './accounts.actions';
import { Account } from './accounts.actions';

describe('AccountsReducer', () => {
  it('should return the default state', () => {
    const action = { type: 'Unknown' } as any;
    const state = accountsReducer(undefined, action);

    expect(state).toBe(initialState);
  });

  it('should set loading to true and clear error on loadAccounts', () => {
    const action = fromActions.loadAccounts();
    const previousState: AccountsState = {
      accounts: [],
      loading: false,
      error: 'some previous error',
    };

    const state = accountsReducer(previousState, action);

    expect(state).toEqual({
      accounts: [],
      loading: true,
      error: null,
    });
  });

  it('should populate accounts and set loading to false on loadAccountsSuccess', () => {
    const mockAccounts: Account[] = [
      {
        uuid: 'uuid-123',
        iban: 'EE1234567890',
        currency: 'EUR',
        balance: 5000,
        user: {} as any,
      },
    ];
    const action = fromActions.loadAccountsSuccess({ accounts: mockAccounts });
    const previousState: AccountsState = { ...initialState, loading: true };

    const state = accountsReducer(previousState, action);

    expect(state).toEqual({
      accounts: mockAccounts,
      loading: false,
      error: null,
    });
  });

  it('should set error and set loading to false on loadAccountsFailure', () => {
    const error = 'Failed to fetch bank accounts';
    const action = fromActions.loadAccountsFailure({ error });
    const previousState: AccountsState = { ...initialState, loading: true };

    const state = accountsReducer(previousState, action);

    expect(state).toEqual({
      accounts: [],
      loading: false,
      error,
    });
  });
});
