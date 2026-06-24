import * as fromSelectors from './account-detail.selectors';
import { AccountDetailState } from './account-detail.reducer';
import { Account } from '../accounts/accounts.actions';

describe('AccountDetail Selectors', () => {
  const mockAccountDetailState: AccountDetailState = {
    account: {
      uuid: 'uuid-123',
      iban: 'EE1234567890',
      currency: 'EUR',
      balance: 4500,
      user: {} as any,
    },
    loading: false,
    error: 'Mock error message',
  };

  // Simula a estrutura do estado global do NgRx contendo a feature 'accountDetail'
  const mockGlobalState = {
    accountDetail: mockAccountDetailState,
  };

  it('should select the feature state (accountDetail)', () => {
    const result = fromSelectors.selectAccountDetailState(mockGlobalState);
    expect(result).toEqual(mockAccountDetailState);
  });

  it('should select the account detail object', () => {
    const result = fromSelectors.selectAccountDetail(mockGlobalState);
    expect(result).toEqual(mockAccountDetailState.account);
  });

  it('should select the loading status', () => {
    const result = fromSelectors.selectAccountDetailLoading(mockGlobalState);
    expect(result).toBe(false);
  });

  it('should select the error message', () => {
    const result = fromSelectors.selectAccountDetailError(mockGlobalState);
    expect(result).toBe('Mock error message');
  });

  it('should return null for account if state is empty', () => {
    const emptyState = {
      accountDetail: {
        account: null,
        loading: false,
        error: null,
      },
    };
    const result = fromSelectors.selectAccountDetail(emptyState);
    expect(result).toBeNull();
  });
});
