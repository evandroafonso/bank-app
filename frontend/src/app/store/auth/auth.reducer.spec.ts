import { authReducer, initialState, AuthState } from './auth.reducer';
import * as fromActions from './auth.actions';
import { AuthUser } from './auth.actions';

describe('AuthReducer', () => {
  it('should return the default state', () => {
    const action = { type: 'Unknown' } as any;
    const state = authReducer(undefined, action);

    expect(state).toBe(initialState);
  });

  it('should set loading to true and clear error on login', () => {
    const credentials = { email: 'evandro@example.com', password: 'password123' };
    const action = fromActions.login({ credentials });
    const previousState: AuthState = {
      user: null,
      loading: false,
      error: 'previous error',
    };

    const state = authReducer(previousState, action);

    expect(state).toEqual({
      user: null,
      loading: true,
      error: null,
    });
  });

  it('should set user and set loading to false on loginSuccess', () => {
    const mockUser: AuthUser = {
      token: 'jwt-token-123',
      email: 'evandro@example.com',
    };
    const action = fromActions.loginSuccess({ user: mockUser });
    const previousState: AuthState = { ...initialState, loading: true };

    const state = authReducer(previousState, action);

    expect(state).toEqual({
      user: mockUser,
      loading: false,
      error: null,
    });
  });

  it('should clear user, set error, and set loading to false on loginFailure', () => {
    const error = 'Invalid email or password.';
    const action = fromActions.loginFailure({ error });
    const previousState: AuthState = {
      user: { token: 'old-token' },
      loading: true,
      error: null,
    };

    const state = authReducer(previousState, action);

    expect(state).toEqual({
      user: null,
      loading: false,
      error,
    });
  });

  it('should clear user and reset loading/error status on logout', () => {
    const action = fromActions.logout();
    const previousState: AuthState = {
      user: { token: 'active-token', email: 'evandro@example.com' },
      loading: false,
      error: 'some error',
    };

    const state = authReducer(previousState, action);

    expect(state).toEqual({
      user: null,
      loading: false,
      error: null,
    });
  });
});
