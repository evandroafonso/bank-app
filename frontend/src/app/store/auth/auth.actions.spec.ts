import * as fromActions from './auth.actions';
import { LoginCredentials, AuthUser } from './auth.actions';

describe('Auth Actions', () => {
  it('should create login action', () => {
    const credentials: LoginCredentials = {
      email: 'evandro@example.com',
      password: 'securePassword123',
    };
    const action = fromActions.login({ credentials });

    expect(action.type).toBe('[Auth] Login');
    expect(action.credentials).toEqual(credentials);
  });

  it('should create loginSuccess action', () => {
    const mockUser: AuthUser = {
      token: 'mock-jwt-token-xyz',
      email: 'evandro@example.com',
    };
    const action = fromActions.loginSuccess({ user: mockUser });

    expect(action.type).toBe('[Auth] Login Success');
    expect(action.user).toEqual(mockUser);
  });

  it('should create loginFailure action', () => {
    const error = 'Invalid credentials';
    const action = fromActions.loginFailure({ error });

    expect(action.type).toBe('[Auth] Login Failure');
    expect(action.error).toBe(error);
  });

  it('should create logout action', () => {
    const action = fromActions.logout();

    expect(action.type).toBe('[Auth] Logout');
  });
});
