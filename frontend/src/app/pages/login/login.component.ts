import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Store } from '@ngrx/store';
import { Observable, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { CommonModule } from '@angular/common';

import { login } from '../../store/auth/auth.actions';
import {
  selectAuthLoading,
  selectAuthError,
  selectIsAuthenticated,
} from '../../store/auth/auth.selectors';
import { ActivatedRoute, Router } from '@angular/router';
import { UsersService } from '../../services/users.service';
import { getApiErrorMessage } from '../../utils/api-error.util';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm!: FormGroup;
  registerForm!: FormGroup;
  loading$: Observable<boolean>;
  error$: Observable<string | null>;
  showPassword = false;
  showRegisterPassword = false;
  isRegisterMode = false;
  registerLoading = false;
  registerError: string | null = null;
  registerSuccess: string | null = null;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private store: Store,
    private router: Router,
    private route: ActivatedRoute,
    private usersService: UsersService,
  ) {
    this.loading$ = this.store.select(selectAuthLoading);
    this.error$ = this.store.select(selectAuthError);
  }

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });

    this.registerForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(120)]],
      personalId: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(32)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });

    // Redirect if already authenticated
    this.store
      .select(selectIsAuthenticated)
      .pipe(takeUntil(this.destroy$))
      .subscribe((isAuth) => {
        if (isAuth) {
          this.router.navigateByUrl(this.getSafeReturnUrl());
        }
      });
  }

  get email() {
    return this.loginForm.get('email');
  }

  get password() {
    return this.loginForm.get('password');
  }

  get username() {
    return this.registerForm.get('username');
  }

  get personalId() {
    return this.registerForm.get('personalId');
  }

  get registerEmail() {
    return this.registerForm.get('email');
  }

  get registerPassword() {
    return this.registerForm.get('password');
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  toggleRegisterPassword(): void {
    this.showRegisterPassword = !this.showRegisterPassword;
  }

  showRegisterForm(): void {
    this.isRegisterMode = true;
    this.registerError = null;
    this.registerSuccess = null;
  }

  showLoginForm(): void {
    this.isRegisterMode = false;
    this.registerError = null;
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.store.dispatch(login({ credentials: this.loginForm.value }));
    } else {
      this.loginForm.markAllAsTouched();
    }
  }

  onRegisterSubmit(): void {
    if (this.registerForm.invalid || this.registerLoading) {
      this.registerForm.markAllAsTouched();
      return;
    }

    const rawValue = this.registerForm.getRawValue();
    const payload = {
      username: rawValue.username.trim(),
      personalId: rawValue.personalId.trim(),
      email: rawValue.email.trim(),
      password: rawValue.password,
    };

    this.registerLoading = true;
    this.registerError = null;
    this.registerSuccess = null;

    this.usersService
      .createUser(payload)
      .pipe(finalize(() => (this.registerLoading = false)))
      .subscribe({
        next: () => {
          this.registerSuccess = 'User created successfully. Sign in to continue.';
          this.registerForm.reset();
          this.isRegisterMode = false;
          this.router.navigate(['/login']);
        },
        error: (error) => {
          this.registerError = getApiErrorMessage(error, 'Unable to create user. Please try again.');
        },
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private getSafeReturnUrl(): string {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');

    return returnUrl?.startsWith('/') && !returnUrl.startsWith('//') ? returnUrl : '/dashboard';
  }
}
