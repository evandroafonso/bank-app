import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CreateUserRequest {
  username: string;
  personalId: string;
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  private readonly apiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient) {}

  createUser(payload: CreateUserRequest): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/create`, payload);
  }
}
