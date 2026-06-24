import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { AccountsService } from '../../services/accounts.service';
import { getApiErrorMessage } from '../../utils/api-error.util';
import { loadAccounts, loadAccountsSuccess, loadAccountsFailure } from './accounts.actions';

@Injectable()
export class AccountsEffects {
  loadAccounts$;

  constructor(
    private actions$: Actions,
    private accountsService: AccountsService,
  ) {
    this.loadAccounts$ = createEffect(() =>
      this.actions$.pipe(
        ofType(loadAccounts),
        switchMap(() =>
          this.accountsService.getAccounts().pipe(
            map((accounts) => loadAccountsSuccess({ accounts })),
            catchError((error) =>
              of(
                loadAccountsFailure({
                  error: getApiErrorMessage(
                    error,
                    'Unable to load accounts. Please try again.',
                  ),
                }),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
