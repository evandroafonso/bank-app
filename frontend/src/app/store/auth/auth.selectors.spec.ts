import * as fromSelectors from './auth.selectors';
import { AuthState } from './auth.reducer';

describe('Auth Selectors', () => {
  const mockAuthState: AuthState = {
    user: {
      token: 'mock-jwt-token-xyz',
      email: 'evandro@example.com',
    },
    loading: false,
    error: 'Invalid password',
  };

  const mockGlobalState = {
    auth: mockAuthState,
  };

  it('should select the feature state (auth)', () => {
    const result = fromSelectors.selectAuthState(mockGlobalState);
    expect(result).toEqual(mockAuthState);
  });

  it('should select the user object', () => {
    const result = fromSelectors.selectUser(mockGlobalState);
    expect(result).toEqual(mockAuthState.user);
  });

  it('should select isAuthenticated as true when user is present', () => {
    const result = fromSelectors.selectIsAuthenticated(mockGlobalState);
    expect(result).toBe(true);
  });

  it('should select isAuthenticated as false when user is null', () => {
    const stateWithoutUser = {
      auth: {
        ...mockAuthState,
        user: null,
      },
    };
    const result = fromSelectors.selectIsAuthenticated(stateWithoutUser);
    expect(result).toBe(false);
  });

  it('should select the loading status', () => {
    const result = fromSelectors.selectAuthLoading(mockGlobalState);
    expect(result).toBe(false);
  });

  it('should select the error message', () => {
    const result = fromSelectors.selectAuthError(mockGlobalState);
    expect(result).toBe('Invalid password');
  });

  it('should select the token string when user exists', () => {
    const result = fromSelectors.selectToken(mockGlobalState);
    expect(result).toBe('mock-jwt-token-xyz');
  });

  it('should return null for selectToken if user is null', () => {
    const stateWithoutUser = {
      auth: {
        ...mockAuthState,
        user: null,
      },
    };
    const result = fromSelectors.selectToken(stateWithoutUser);
    expect(result).toBeNull();
  });
});
