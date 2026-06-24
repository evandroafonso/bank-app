import * as fromSelectors from './accounts.selectors';
import { AccountsState } from './accounts.reducer';
import { Account } from './accounts.actions';

describe('Accounts Selectors', () => {
  const mockAccountsState: AccountsState = {
    accounts: [
      {
        uuid: 'uuid-1',
        iban: 'EE1234567890',
        currency: 'EUR',
        balance: 1500.5,
        user: {} as any,
      },
      {
        uuid: 'uuid-2',
        iban: 'EE0987654321',
        currency: 'EUR',
        balance: 2500.0,
        user: {} as any,
      },
    ],
    loading: false,
    error: 'Failed to connection',
  };

  const mockGlobalState = {
    accounts: mockAccountsState,
  };

  it('should select the feature state (accounts)', () => {
    const result = fromSelectors.selectAccountsState(mockGlobalState);
    expect(result).toEqual(mockAccountsState);
  });

  it('should select the accounts list', () => {
    const result = fromSelectors.selectAccounts(mockGlobalState);
    expect(result).toEqual(mockAccountsState.accounts);
    expect(result.length).toBe(2);
  });

  it('should select the loading status', () => {
    const result = fromSelectors.selectAccountsLoading(mockGlobalState);
    expect(result).toBe(false);
  });

  it('should select the error message', () => {
    const result = fromSelectors.selectAccountsError(mockGlobalState);
    expect(result).toBe('Failed to connection');
  });

  it('should calculate the total balance of all accounts combined', () => {
    const result = fromSelectors.selectTotalBalance(mockGlobalState);
    expect(result).toBe(4000.5);
  });

  it('should return 0 for total balance if there are no accounts', () => {
    const emptyState = {
      accounts: {
        accounts: [],
        loading: false,
        error: null,
      },
    };
    const result = fromSelectors.selectTotalBalance(emptyState);
    expect(result).toBe(0);
  });
});
