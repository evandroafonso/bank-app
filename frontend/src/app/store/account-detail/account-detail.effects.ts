import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { AccountsService } from '../../services/accounts.service';
import {
  loadAccountDetail,
  loadAccountDetailFailure,
  loadAccountDetailSuccess,
} from './account-detail.actions';

@Injectable()
export class AccountDetailEffects {
  loadAccountDetail$;

  constructor(
    private actions$: Actions,
    private accountsService: AccountsService,
  ) {
    this.loadAccountDetail$ = createEffect(() =>
      this.actions$.pipe(
        ofType(loadAccountDetail),
        switchMap(({ iban }) =>
          this.accountsService.getAccountByIban(iban).pipe(
            map((account) => loadAccountDetailSuccess({ account })),
            catchError(() =>
              of(loadAccountDetailFailure({ error: 'Erro ao carregar detalhes da conta.' })),
            ),
          ),
        ),
      ),
    );
  }
}
