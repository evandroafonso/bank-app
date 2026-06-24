import { ApplicationConfig, isDevMode } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideStore } from '@ngrx/store';
import { provideEffects } from '@ngrx/effects';
import { provideStoreDevtools } from '@ngrx/store-devtools';

import { routes } from './app.routes';
import { authReducer } from './store/auth/auth.reducer';
import { AuthEffects } from './store/auth/auth.effects';
import { accountsReducer } from './store/accounts/accounts.reducer';
import { AccountsEffects } from './store/accounts/accounts.effects';
import { accountDetailReducer } from './store/account-overview/account-detail.reducer';
import { AccountDetailEffects } from './store/account-overview/account-detail.effects';
import { transactionsReducer } from './store/transactions/transactions.reducer';
import { TransactionsEffects } from './store/transactions/transactions.effects';
import { transactionDetailReducer } from './store/transaction-detail/transaction-detail.reducer';
import { TransactionDetailEffects } from './store/transaction-detail/transaction-detail.effects';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(withFetch()),
    provideStore({
      auth: authReducer,
      accounts: accountsReducer,
      accountDetail: accountDetailReducer,
      transactions: transactionsReducer,
      transactionDetail: transactionDetailReducer,
    }),
    provideEffects([
      AuthEffects,
      AccountsEffects,
      AccountDetailEffects,
      TransactionsEffects,
      TransactionDetailEffects,
    ]),
    provideStoreDevtools({ maxAge: 25, logOnly: !isDevMode() }),
  ],
};
