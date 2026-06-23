import { createReducer, on } from '@ngrx/store';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID, inject } from '@angular/core';
import { login, loginSuccess, loginFailure, logout, AuthUser } from './auth.actions';

export interface AuthState {
  user: AuthUser | null;
  loading: boolean;
  error: string | null;
}

const isBrowser = typeof window !== 'undefined' && typeof localStorage !== 'undefined';

export const initialState: AuthState = {
  user:
    isBrowser && localStorage.getItem('auth_token')
      ? { token: localStorage.getItem('auth_token')! }
      : null,
  loading: false,
  error: null,
};

export const authReducer = createReducer(
  initialState,

  on(login, (state) => ({
    ...state,
    loading: true,
    error: null,
  })),

  on(loginSuccess, (state, { user }) => ({
    ...state,
    user,
    loading: false,
    error: null,
  })),

  on(loginFailure, (state, { error }) => ({
    ...state,
    user: null,
    loading: false,
    error,
  })),

  on(logout, () => ({
    user: null,
    loading: false,
    error: null,
  })),
);
