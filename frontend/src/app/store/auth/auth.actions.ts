import { createAction, props } from '@ngrx/store';

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface AuthUser {
  token: string;
  email?: string;
}

export const login = createAction('[Auth] Login', props<{ credentials: LoginCredentials }>());

export const loginSuccess = createAction('[Auth] Login Success', props<{ user: AuthUser }>());

export const loginFailure = createAction('[Auth] Login Failure', props<{ error: string }>());

export const logout = createAction('[Auth] Logout');
