import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { TransactionsService } from '../../services/transactions.service';
import {
  loadTransactions,
  loadTransactionsSuccess,
  loadTransactionsFailure,
} from './transactions.actions';

@Injectable()
export class TransactionsEffects {
  loadTransactions$;

  constructor(
    private actions$: Actions,
    private transactionsService: TransactionsService,
  ) {
    this.loadTransactions$ = createEffect(() =>
      this.actions$.pipe(
        ofType(loadTransactions),
        switchMap(({ iban, page, size }) =>
          this.transactionsService.getTransactionHistory(iban, page, size).pipe(
            map((data) => loadTransactionsSuccess({ data })),
            catchError(() =>
              of(loadTransactionsFailure({ error: 'Unable to load transactions.' })),
            ),
          ),
        ),
      ),
    );
  }
}
