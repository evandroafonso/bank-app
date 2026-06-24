import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoginCredentials, AuthUser } from '../store/auth/auth.actions';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(credentials: LoginCredentials): Observable<AuthUser> {
    return this.http.post<AuthUser>(`${this.apiUrl}/login`, credentials);
  }

  saveToken(token: string): void {
    if (!this.hasLocalStorage()) {
      return;
    }

    localStorage.setItem('auth_token', token);
  }

  getToken(): string | null {
    if (!this.hasLocalStorage()) {
      return null;
    }

    return localStorage.getItem('auth_token');
  }

  removeToken(): void {
    if (!this.hasLocalStorage()) {
      return;
    }

    localStorage.removeItem('auth_token');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  private hasLocalStorage(): boolean {
    return typeof localStorage !== 'undefined';
  }
}
