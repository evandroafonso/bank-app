import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { provideMockActions } from '@ngrx/effects/testing';
import { Observable, of, throwError } from 'rxjs';
import { AuthEffects } from './auth.effects';
import { AuthService } from '../../services/auth.service';
import { login, loginSuccess, loginFailure, logout } from './auth.actions';
import { AuthUser, LoginCredentials } from './auth.actions';

describe('AuthEffects', () => {
  let actions$: Observable<any>;
  let effects: AuthEffects;
  let authServiceMock: jest.Mocked<AuthService>;
  let routerMock: jest.Mocked<Router>;

  beforeEach(() => {
    const authSpy = {
      login: jest.fn(),
      saveToken: jest.fn(),
      removeToken: jest.fn(),
    };

    const routerSpy = {
      navigate: jest.fn(),
      navigateByUrl: jest.fn(),
      parseUrl: jest.fn(),
      url: '/login',
    };

    TestBed.configureTestingModule({
      providers: [
        AuthEffects,
        provideMockActions(() => actions$),
        { provide: AuthService, useValue: authSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });

    effects = TestBed.inject(AuthEffects);
    authServiceMock = TestBed.inject(AuthService) as jest.Mocked<AuthService>;
    routerMock = TestBed.inject(Router) as jest.Mocked<Router>;
  });

  it('should be created', () => {
    expect(effects).toBeTruthy();
  });

  describe('login$', () => {
    const credentials: LoginCredentials = { email: 'evandro@example.com', password: 'password123' };

    it('should return loginSuccess with user on successful authentication', (done) => {
      const mockUser: AuthUser = { token: 'jwt-token-123', email: 'evandro@example.com' };
      actions$ = of(login({ credentials }));
      authServiceMock.login.mockReturnValue(of(mockUser));

      effects.login$.subscribe((action) => {
        expect(action).toEqual(loginSuccess({ user: mockUser }));
        expect(authServiceMock.login).toHaveBeenCalledWith(credentials);
        done();
      });
    });

    it('should return loginFailure with specific message on 401 unauthorized error', (done) => {
      const mockError = { status: 401 };
      actions$ = of(login({ credentials }));
      authServiceMock.login.mockReturnValue(throwError(() => mockError));

      effects.login$.subscribe((action) => {
        const failureAction = action as ReturnType<typeof loginFailure>;
        expect(failureAction.type).toBe(loginFailure.type);
        expect(failureAction.error).toBe('Invalid email or password.');
        done();
      });
    });

    it('should return loginFailure with fallback message on generic connection error', (done) => {
      const mockError = { status: 500 };
      actions$ = of(login({ credentials }));
      authServiceMock.login.mockReturnValue(throwError(() => mockError));

      effects.login$.subscribe((action) => {
        const failureAction = action as ReturnType<typeof loginFailure>;
        expect(failureAction.type).toBe(loginFailure.type);
        expect(failureAction.error).toBe('Unable to connect to the server. Please try again.');
        done();
      });
    });
  });

  describe('loginSuccess$', () => {
    const mockUser: AuthUser = { token: 'jwt-token-123', email: 'evandro@example.com' };

    it('should save token and navigate to /dashboard when no valid returnUrl is provided', (done) => {
      actions$ = of(loginSuccess({ user: mockUser }));
      routerMock.parseUrl.mockReturnValue({ queryParams: {} } as any);

      effects.loginSuccess$.subscribe(() => {
        expect(authServiceMock.saveToken).toHaveBeenCalledWith(mockUser.token);
        expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/dashboard');
        done();
      });
    });

    it('should save token and navigate to safe returnUrl when provided', (done) => {
      actions$ = of(loginSuccess({ user: mockUser }));
      routerMock.parseUrl.mockReturnValue({ queryParams: { returnUrl: '/accounts' } } as any);

      effects.loginSuccess$.subscribe(() => {
        expect(authServiceMock.saveToken).toHaveBeenCalledWith(mockUser.token);
        expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/accounts');
        done();
      });
    });

    it('should fallback to /dashboard if returnUrl is unsafe or invalid', (done) => {
      actions$ = of(loginSuccess({ user: mockUser }));
      routerMock.parseUrl.mockReturnValue({
        queryParams: { returnUrl: '//malicious-domain.com' },
      } as any);

      effects.loginSuccess$.subscribe(() => {
        expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/dashboard');
        done();
      });
    });
  });

  describe('logout$', () => {
    it('should remove token and navigate to login page', (done) => {
      actions$ = of(logout());

      effects.logout$.subscribe(() => {
        expect(authServiceMock.removeToken).toHaveBeenCalled();
        expect(routerMock.navigate).toHaveBeenCalledWith(['/login']);
        done();
      });
    });
  });
});
