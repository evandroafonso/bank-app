import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { TransactionsService } from '../../services/transactions.service';
import {
  loadTransactionDetail,
  loadTransactionDetailFailure,
  loadTransactionDetailSuccess,
} from './transaction-detail.actions';

@Injectable()
export class TransactionDetailEffects {
  loadTransactionDetail$;

  constructor(
    private actions$: Actions,
    private transactionsService: TransactionsService,
  ) {
    this.loadTransactionDetail$ = createEffect(() =>
      this.actions$.pipe(
        ofType(loadTransactionDetail),
        switchMap(({ uuid }) =>
          this.transactionsService.getTransactionByUuid(uuid).pipe(
            map((transaction) => loadTransactionDetailSuccess({ transaction })),
            catchError(() =>
              of(loadTransactionDetailFailure({ error: 'Unable to load transaction details.' })),
            ),
          ),
        ),
      ),
    );
  }
}
