import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';

import { AuthService } from '../../services/auth.service';
import { getApiErrorMessage } from '../../utils/api-error.util';
import { login, loginSuccess, loginFailure, logout } from './auth.actions';

@Injectable()
export class AuthEffects {
  login$;
  loginSuccess$;
  logout$;

  constructor(
    private actions$: Actions,
    private authService: AuthService,
    private router: Router,
  ) {
    this.login$ = createEffect(() =>
      this.actions$.pipe(
        ofType(login),
        switchMap(({ credentials }) =>
          this.authService.login(credentials).pipe(
            map((user) => loginSuccess({ user })),
            catchError((error) => {
              const fallbackMessage =
                error.status === 401
                  ? 'Invalid email or password.'
                  : 'Unable to connect to the server. Please try again.';
              const message = getApiErrorMessage(error, fallbackMessage);

              return of(loginFailure({ error: message }));
            }),
          ),
        ),
      ),
    );

    this.loginSuccess$ = createEffect(
      () =>
        this.actions$.pipe(
          ofType(loginSuccess),
          tap(({ user }) => {
            this.authService.saveToken(user.token);
            const returnUrl = this.router.parseUrl(this.router.url).queryParams['returnUrl'];
            this.router.navigateByUrl(this.getSafeReturnUrl(returnUrl));
          }),
        ),
      { dispatch: false },
    );

    this.logout$ = createEffect(
      () =>
        this.actions$.pipe(
          ofType(logout),
          tap(() => {
            this.authService.removeToken();
            this.router.navigate(['/login']);
          }),
        ),
      { dispatch: false },
    );
  }

  private getSafeReturnUrl(returnUrl: unknown): string {
    return typeof returnUrl === 'string' && returnUrl.startsWith('/') && !returnUrl.startsWith('//')
      ? returnUrl
      : '/dashboard';
  }
}
