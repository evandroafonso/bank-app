import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction, TransactionPage } from '../store/transactions/transactions.actions';
import { AuthService } from './auth.service';

export type TransactionOperationType = 'CREDIT' | 'DEBIT';

export interface TransactionOperationRequest {
  iban: string;
  amount: number;
  description: string;
  currency: string;
}

@Injectable({
  providedIn: 'root',
})
export class TransactionsService {
  private readonly apiUrl = 'http://localhost:8080/api/transactions';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken() ?? localStorage.getItem('auth_token') ?? ''}`,
    });
  }

  getBalanceChart(
    iban: string,
    startDate: string,
    endDate: string,
  ): Observable<Array<{ timestamp: string; balance: number }>> {
    const params = new HttpParams()
      .set('iban', iban)
      .set('startDate', startDate)
      .set('endDate', endDate);

    const url = `${this.apiUrl}/balance-chart`;
    // eslint-disable-next-line no-console
    console.debug('[TransactionsService] getBalanceChart URL:', url, { iban, startDate, endDate });

    return this.http.get<Array<{ timestamp: string; balance: number }>>(url, {
      headers: this.getHeaders(),
      params,
    });
  }

  getTransactionHistory(iban: string, page: number, size: number): Observable<TransactionPage> {
    const params = new HttpParams().set('page', page.toString()).set('size', size.toString());

    return this.http.get<TransactionPage>(`${this.apiUrl}/history/${iban}`, {
      headers: this.getHeaders(),
      params,
    });
  }

  getTransactionByUuid(uuid: string): Observable<Transaction> {
    return this.http.get<Transaction>(`${this.apiUrl}/${uuid}`, {
      headers: this.getHeaders(),
    });
  }

  creditAccount(payload: TransactionOperationRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}/credit`, payload, {
      headers: this.getHeaders(),
    });
  }

  debitAccount(payload: TransactionOperationRequest): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}/debit`, payload, {
      headers: this.getHeaders(),
    });
  }

  exportTransactionPdf(transactionUuid: string): Observable<Blob> {
    const token = localStorage.getItem('auth_token');
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });

    return this.http.post(
      'http://localhost:8080/api/reports/transaction/pdf',
      { transactionUuid },
      {
        headers: headers,
        responseType: 'blob',
      },
    );
  }
}
