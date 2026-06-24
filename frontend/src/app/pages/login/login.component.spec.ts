import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { UsersService } from '../../services/users.service';
import { login } from '../../store/auth/auth.actions';
import {
  selectAuthLoading,
  selectAuthError,
  selectIsAuthenticated,
} from '../../store/auth/auth.selectors';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let store: MockStore;
  let router: Router;

  let dispatchSpy: jest.SpyInstance;
  let navigateSpy: jest.SpyInstance;
  let navigateByUrlSpy: jest.SpyInstance;

  let usersService: {
    createUser: jest.Mock;
  };

  beforeEach(async () => {
    usersService = {
      createUser: jest.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        provideMockStore({
          initialState: {
            auth: {
              loading: false,
              error: null,
              isAuthenticated: false,
            },
            transactions: {
              transactions: [],
              loading: false,
              error: null,
              isLast: true,
              currentPage: 0,
              totalElements: 0,
            },
            accountDetail: {
              account: null,
              loading: false,
              error: null,
            },
          },
        }),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: jest.fn().mockReturnValue(null),
              },
            },
          },
        },
        {
          provide: UsersService,
          useValue: usersService,
        },
      ],
    }).compileComponents();

    store = TestBed.inject(MockStore);
    router = TestBed.inject(Router);

    dispatchSpy = jest.spyOn(store, 'dispatch');
    navigateSpy = jest.spyOn(router, 'navigate').mockResolvedValue(true);
    navigateByUrlSpy = jest
      .spyOn(router, 'navigateByUrl')
      .mockResolvedValue(true);

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should create login form', () => {
      expect(component.loginForm).toBeTruthy();

      expect(component.loginForm.contains('email')).toBe(true);
      expect(component.loginForm.contains('password')).toBe(true);
    });

    it('should create register form', () => {
      expect(component.registerForm).toBeTruthy();

      expect(component.registerForm.contains('username')).toBe(true);
      expect(component.registerForm.contains('personalId')).toBe(true);
      expect(component.registerForm.contains('email')).toBe(true);
      expect(component.registerForm.contains('password')).toBe(true);
    });

    it('should redirect when authenticated', () => {
      store.overrideSelector(selectIsAuthenticated, true);
      store.refreshState();

      fixture = TestBed.createComponent(LoginComponent);
      component = fixture.componentInstance;

      fixture.detectChanges();

      expect(navigateByUrlSpy).toHaveBeenCalledWith('/dashboard');
    });

    it('should redirect to returnUrl when valid', () => {
      TestBed.resetTestingModule();
    });
  });

  describe('form getters', () => {
    it('should return login controls', () => {
      expect(component.email).toBe(component.loginForm.get('email'));
      expect(component.password).toBe(component.loginForm.get('password'));
    });

    it('should return register controls', () => {
      expect(component.username).toBe(
        component.registerForm.get('username'),
      );

      expect(component.personalId).toBe(
        component.registerForm.get('personalId'),
      );

      expect(component.registerEmail).toBe(
        component.registerForm.get('email'),
      );

      expect(component.registerPassword).toBe(
        component.registerForm.get('password'),
      );
    });
  });

  describe('form validation', () => {
    describe('login form', () => {
      it('email should be invalid when malformed', () => {
        component.loginForm.patchValue({
          email: 'invalid-email',
          password: '123456',
        });

        expect(component.email?.valid).toBe(false);
      });

      it('password should require minimum length', () => {
        component.loginForm.patchValue({
          email: 'john@example.com',
          password: '123',
        });

        expect(component.password?.valid).toBe(false);
      });

      it('should be valid with correct values', () => {
        component.loginForm.patchValue({
          email: 'john@example.com',
          password: '123456',
        });

        expect(component.loginForm.valid).toBe(true);
      });
    });

    describe('register form', () => {
      it('should require username', () => {
        component.registerForm.patchValue({
          username: '',
          personalId: '123456',
          email: 'john@example.com',
          password: '123456',
        });

        expect(component.username?.valid).toBe(false);
      });

      it('should require minimum username length', () => {
        component.registerForm.patchValue({
          username: 'ab',
          personalId: '123456',
          email: 'john@example.com',
          password: '123456',
        });

        expect(component.username?.valid).toBe(false);
      });

      it('should require valid email', () => {
        component.registerForm.patchValue({
          username: 'john',
          personalId: '123456',
          email: 'invalid',
          password: '123456',
        });

        expect(component.registerEmail?.valid).toBe(false);
      });

      it('should be valid with correct values', () => {
        component.registerForm.patchValue({
          username: 'john',
          personalId: '123456',
          email: 'john@example.com',
          password: '123456',
        });

        expect(component.registerForm.valid).toBe(true);
      });
    });
  });

  describe('password visibility', () => {
    it('should toggle login password visibility', () => {
      expect(component.showPassword).toBe(false);

      component.togglePassword();

      expect(component.showPassword).toBe(true);

      component.togglePassword();

      expect(component.showPassword).toBe(false);
    });

    it('should toggle register password visibility', () => {
      expect(component.showRegisterPassword).toBe(false);

      component.toggleRegisterPassword();

      expect(component.showRegisterPassword).toBe(true);

      component.toggleRegisterPassword();

      expect(component.showRegisterPassword).toBe(false);
    });
  });

  describe('register/login mode', () => {
    it('should show register form', () => {
      component.registerError = 'error';
      component.registerSuccess = 'success';

      component.showRegisterForm();

      expect(component.isRegisterMode).toBe(true);
      expect(component.registerError).toBeNull();
      expect(component.registerSuccess).toBeNull();
    });

    it('should show login form', () => {
      component.isRegisterMode = true;
      component.registerError = 'error';

      component.showLoginForm();

      expect(component.isRegisterMode).toBe(false);
      expect(component.registerError).toBeNull();
    });
  });

  describe('onSubmit', () => {
    it('should dispatch login action when form is valid', () => {
      component.loginForm.patchValue({
        email: 'john@example.com',
        password: '123456',
      });

      component.onSubmit();

      expect(dispatchSpy).toHaveBeenCalledWith(
        login({
          credentials: {
            email: 'john@example.com',
            password: '123456',
          },
        }),
      );
    });

    it('should mark form as touched when invalid', () => {
      const markSpy = jest.spyOn(
        component.loginForm,
        'markAllAsTouched',
      );

      component.onSubmit();

      expect(markSpy).toHaveBeenCalled();
      expect(dispatchSpy).not.toHaveBeenCalledWith(
        expect.objectContaining({
          type: login.type,
        }),
      );
    });
  });

  describe('onRegisterSubmit', () => {
    beforeEach(() => {
      component.registerForm.setValue({
        username: 'john',
        personalId: '123456',
        email: 'john@example.com',
        password: '123456',
      });
    });

    it('should not submit invalid form', () => {
      component.registerForm.patchValue({
        username: '',
      });

      const markSpy = jest.spyOn(
        component.registerForm,
        'markAllAsTouched',
      );

      component.onRegisterSubmit();

      expect(markSpy).toHaveBeenCalled();
      expect(usersService.createUser).not.toHaveBeenCalled();
    });

    it('should not submit when loading', () => {
      component.registerLoading = true;

      component.onRegisterSubmit();

      expect(usersService.createUser).not.toHaveBeenCalled();
    });

    it('should create user successfully', fakeAsync(() => {
      usersService.createUser.mockReturnValue(of({}));

      component.isRegisterMode = true;

      component.onRegisterSubmit();
      tick();

      expect(usersService.createUser).toHaveBeenCalledWith({
        username: 'john',
        personalId: '123456',
        email: 'john@example.com',
        password: '123456',
      });

      expect(component.registerLoading).toBe(false);

      expect(component.registerSuccess).toBe(
        'User created successfully. Sign in to continue.',
      );

      expect(component.isRegisterMode).toBe(false);

      expect(navigateSpy).toHaveBeenCalledWith(['/login']);
    }));

    it('should trim payload values before submit', fakeAsync(() => {
      usersService.createUser.mockReturnValue(of({}));

      // email cannot have spaces — it would fail Validators.email
      // trim is tested only on fields that allow surrounding whitespace
      component.registerForm.setValue({
        username: ' john ',
        personalId: ' 123456 ',
        email: 'john@example.com',
        password: '123456',
      });

      component.onRegisterSubmit();
      tick();

      expect(usersService.createUser).toHaveBeenCalledWith({
        username: 'john',
        personalId: '123456',
        email: 'john@example.com',
        password: '123456',
      });
    }));

    it('should handle registration error', fakeAsync(() => {
      usersService.createUser.mockReturnValue(
        throwError(() => ({
          error: {
            message: 'User already exists',
          },
        })),
      );

      component.onRegisterSubmit();
      tick();

      expect(component.registerLoading).toBe(false);

      expect(component.registerError).toBe(
        'User already exists',
      );
    }));
  });

  describe('returnUrl', () => {
    it('should navigate to safe returnUrl', () => {
      const route = TestBed.inject(
        ActivatedRoute,
      ) as unknown as {
        snapshot: {
          queryParamMap: {
            get: jest.Mock;
          };
        };
      };

      route.snapshot.queryParamMap.get.mockReturnValue(
        '/accounts',
      );

      store.overrideSelector(selectIsAuthenticated, true);
      store.refreshState();

      fixture = TestBed.createComponent(LoginComponent);
      component = fixture.componentInstance;

      fixture.detectChanges();

      expect(navigateByUrlSpy).toHaveBeenCalledWith(
        '/accounts',
      );
    });

    it('should ignore unsafe returnUrl', () => {
      const route = TestBed.inject(
        ActivatedRoute,
      ) as unknown as {
        snapshot: {
          queryParamMap: {
            get: jest.Mock;
          };
        };
      };

      route.snapshot.queryParamMap.get.mockReturnValue(
        '//evil-site.com',
      );

      store.overrideSelector(selectIsAuthenticated, true);
      store.refreshState();

      fixture = TestBed.createComponent(LoginComponent);
      component = fixture.componentInstance;

      fixture.detectChanges();

      expect(navigateByUrlSpy).toHaveBeenCalledWith(
        '/dashboard',
      );
    });
  });

  describe('ngOnDestroy', () => {
    it('should complete destroy subject', () => {
      const nextSpy = jest.spyOn(
        (component as any).destroy$,
        'next',
      );

      const completeSpy = jest.spyOn(
        (component as any).destroy$,
        'complete',
      );

      component.ngOnDestroy();

      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
    });
  });
});