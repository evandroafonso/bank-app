import * as fromActions from './account-detail.actions';
import { Account } from '../accounts/accounts.actions';

describe('Account Detail Actions', () => {
  it('should create loadAccountDetail action', () => {
    const iban = 'EE1234567890';
    const action = fromActions.loadAccountDetail({ iban });

    expect(action.type).toBe('[Account Detail] Load Account Detail');
    expect(action.iban).toBe(iban);
  });

  it('should create loadAccountDetailSuccess action', () => {
    const mockAccount: Account = {
      uuid: 'uuid-123',
      iban: 'EE1234567890',
      currency: 'EUR',
      balance: 1500,
      user: {} as any,
    };
    const action = fromActions.loadAccountDetailSuccess({ account: mockAccount });

    expect(action.type).toBe('[Account Detail] Load Account Detail Success');
    expect(action.account).toEqual(mockAccount);
  });

  it('should create loadAccountDetailFailure action', () => {
    const error = 'Server Error';
    const action = fromActions.loadAccountDetailFailure({ error });

    expect(action.type).toBe('[Account Detail] Load Account Detail Failure');
    expect(action.error).toBe(error);
  });

  it('should create clearAccountDetail action', () => {
    const action = fromActions.clearAccountDetail();

    expect(action.type).toBe('[Account Detail] Clear');
  });
});
