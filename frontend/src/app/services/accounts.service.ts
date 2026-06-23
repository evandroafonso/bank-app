import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Account } from '../store/accounts/accounts.actions';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class AccountsService {
  private readonly apiUrl = 'http://localhost:8080/api/accounts';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`,
    });
  }

  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getAccountByIban(iban: string): Observable<Account> {
    return this.http.get<Account>(`${this.apiUrl}/${iban}`, { headers: this.getHeaders() });
  }
}
