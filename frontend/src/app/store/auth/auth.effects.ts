import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';

import { AuthService } from '../../services/auth.service';
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
              const message =
                error.status === 401
                  ? 'E-mail ou senha inválidos.'
                  : 'Erro ao conectar ao servidor. Tente novamente.';
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
            this.router.navigate(['/dashboard']);
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
}
