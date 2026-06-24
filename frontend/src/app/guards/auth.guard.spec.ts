import { TestBed } from '@angular/core/testing';
import { Router, RouterStateSnapshot, ActivatedRouteSnapshot, UrlTree } from '@angular/router';
import { Store } from '@ngrx/store';
import { of, Observable } from 'rxjs';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

describe('authGuard', () => {
  let storeMock: { select: jest.Mock };
  let routerMock: { createUrlTree: jest.Mock };
  let authServiceMock: { isAuthenticated: jest.Mock };

  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: '/dashboard' } as RouterStateSnapshot;
  const mockUrlTree = {} as UrlTree;

  beforeEach(() => {
    storeMock = { select: jest.fn() };
    routerMock = { createUrlTree: jest.fn().mockReturnValue(mockUrlTree) };
    authServiceMock = { isAuthenticated: jest.fn() };

    TestBed.configureTestingModule({
      providers: [
        { provide: Store, useValue: storeMock },
        { provide: Router, useValue: routerMock },
        { provide: AuthService, useValue: authServiceMock },
      ],
    });
  });

  it('should allow activation when user is authenticated via Store', (done) => {
    storeMock.select.mockReturnValue(of(true));
    authServiceMock.isAuthenticated.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState),
    ) as Observable<boolean | UrlTree>;

    result.subscribe((allowed) => {
      expect(allowed).toBe(true);
      done();
    });
  });

  it('should allow activation when user is authenticated via AuthService only', (done) => {
    storeMock.select.mockReturnValue(of(false));
    authServiceMock.isAuthenticated.mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState),
    ) as Observable<boolean | UrlTree>;

    result.subscribe((allowed) => {
      expect(allowed).toBe(true);
      done();
    });
  });

  it('should redirect to /login with returnUrl when user is not authenticated', (done) => {
    storeMock.select.mockReturnValue(of(false));
    authServiceMock.isAuthenticated.mockReturnValue(false);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState),
    ) as Observable<boolean | UrlTree>;

    result.subscribe((allowed) => {
      expect(allowed).toBe(mockUrlTree);
      expect(routerMock.createUrlTree).toHaveBeenCalledWith(['/login'], {
        queryParams: { returnUrl: '/dashboard' },
      });
      done();
    });
  });
});
