import * as fromActions from './accounts.actions';
import { Account } from './accounts.actions';

describe('Accounts Actions', () => {
  it('should create loadAccounts action', () => {
    const action = fromActions.loadAccounts();

    expect(action.type).toBe('[Accounts] Load Accounts');
  });

  it('should create loadAccountsSuccess action', () => {
    const mockAccounts: Account[] = [
      {
        uuid: 'uuid-123',
        iban: 'EE1234567890',
        currency: 'EUR',
        balance: 1000,
        user: {
          uuid: 'user-999',
          username: 'evandro',
          personalId: '12345678901',
          email: 'evandro@example.com',
        },
      },
    ];
    const action = fromActions.loadAccountsSuccess({ accounts: mockAccounts });

    expect(action.type).toBe('[Accounts] Load Accounts Success');
    expect(action.accounts).toEqual(mockAccounts);
  });

  it('should create loadAccountsFailure action', () => {
    const error = 'Failed to fetch accounts';
    const action = fromActions.loadAccountsFailure({ error });

    expect(action.type).toBe('[Accounts] Load Accounts Failure');
    expect(action.error).toBe(error);
  });
});
