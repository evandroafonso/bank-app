import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Currency {
  code: string;
  description: string;
}

@Injectable({
  providedIn: 'root',
})
export class CurrenciesService {
  private readonly apiUrl = 'http://localhost:8080/api/currencies';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Bearer ${this.authService.getToken()}`,
    });
  }

  getCurrencies(): Observable<Currency[]> {
    return this.http.get<Currency[]>(this.apiUrl, { headers: this.getHeaders() });
  }
}
