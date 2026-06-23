import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction, TransactionPage } from '../store/transactions/transactions.actions';
import { AuthService } from './auth.service';

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
      Authorization: `Bearer ${this.authService.getToken()}`,
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
}
