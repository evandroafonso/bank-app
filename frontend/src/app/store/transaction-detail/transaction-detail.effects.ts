import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

import { TransactionsService } from '../../services/transactions.service';
import { getApiErrorMessage } from '../../utils/api-error.util';
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
            catchError((error) =>
              of(
                loadTransactionDetailFailure({
                  error: getApiErrorMessage(error, 'Unable to load transaction details.'),
                }),
              ),
            ),
          ),
        ),
      ),
    );
  }
}
