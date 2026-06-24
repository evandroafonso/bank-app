import { accountDetailReducer, initialState, AccountDetailState } from './account-detail.reducer';
import * as fromActions from './account-detail.actions';
import { Account } from '../accounts/accounts.actions';

describe('AccountDetailReducer', () => {
  it('should return the default state', () => {
    const action = { type: 'Unknown' } as any;
    const state = accountDetailReducer(undefined, action);

    expect(state).toBe(initialState);
  });

  it('should set loading to true and reset account and error on loadAccountDetail', () => {
    const action = fromActions.loadAccountDetail({ iban: 'EE1234567890' });
    const previousState: AccountDetailState = {
      account: { uuid: 'old' } as Account,
      loading: false,
      error: 'old error',
    };

    const state = accountDetailReducer(previousState, action);

    expect(state).toEqual({
      account: null,
      loading: true,
      error: null,
    });
  });

  it('should populate account and set loading to false on loadAccountDetailSuccess', () => {
    const mockAccount: Account = {
      uuid: 'uuid-123',
      iban: 'EE1234567890',
      currency: 'EUR',
      balance: 3400,
      user: {} as any,
    };
    const action = fromActions.loadAccountDetailSuccess({ account: mockAccount });
    const previousState: AccountDetailState = { ...initialState, loading: true };

    const state = accountDetailReducer(previousState, action);

    expect(state).toEqual({
      account: mockAccount,
      loading: false,
      error: null,
    });
  });

  it('should set error and set loading to false on loadAccountDetailFailure', () => {
    const error = 'Internal Server Error';
    const action = fromActions.loadAccountDetailFailure({ error });
    const previousState: AccountDetailState = { ...initialState, loading: true };

    const state = accountDetailReducer(previousState, action);

    expect(state).toEqual({
      account: null,
      loading: false,
      error,
    });
  });

  it('should reset to initial state on clearAccountDetail', () => {
    const modifiedState: AccountDetailState = {
      account: { uuid: 'uuid-123' } as Account,
      loading: false,
      error: 'some error',
    };
    const action = fromActions.clearAccountDetail();

    const state = accountDetailReducer(modifiedState, action);

    expect(state).toEqual(initialState);
  });
});
