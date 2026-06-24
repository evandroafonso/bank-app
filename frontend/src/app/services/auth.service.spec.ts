import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { LoginCredentials, AuthUser } from '../store/auth/auth.actions';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  const apiUrl = 'http://localhost:8080/api/auth';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);

    jest.spyOn(Storage.prototype, 'setItem');
    jest.spyOn(Storage.prototype, 'getItem');
    jest.spyOn(Storage.prototype, 'removeItem');
  });

  afterEach(() => {
    httpMock.verify();
    jest.clearAllMocks();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should authenticate user via login', () => {
    const credentials: LoginCredentials = { username: 'user', password: 'password' } as any;
    const mockUser: AuthUser = { id: '1', token: 'jwt-token' } as any;

    service.login(credentials).subscribe((user) => {
      expect(user).toEqual(mockUser);
    });

    const req = httpMock.expectOne(`${apiUrl}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credentials);
    req.flush(mockUser);
  });

  it('should save token to localStorage', () => {
    const token = 'my-token';
    service.saveToken(token);

    expect(localStorage.setItem).toHaveBeenCalledWith('auth_token', token);
    expect(localStorage.getItem('auth_token')).toBe(token);
  });

  it('should get token from localStorage', () => {
    const token = 'my-token';
    localStorage.setItem('auth_token', token);

    const result = service.getToken();

    expect(localStorage.getItem).toHaveBeenCalledWith('auth_token');
    expect(result).toBe(token);
  });

  it('should return null when token does not exist', () => {
    const result = service.getToken();
    expect(result).toBeNull();
  });

  it('should remove token from localStorage', () => {
    localStorage.setItem('auth_token', 'my-token');
    service.removeToken();

    expect(localStorage.removeItem).toHaveBeenCalledWith('auth_token');
    expect(localStorage.getItem('auth_token')).toBeNull();
  });

  it('should return true for isAuthenticated when token is present', () => {
    jest.spyOn(service, 'getToken').mockReturnValue('existing-token');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('should return false for isAuthenticated when token is missing', () => {
    jest.spyOn(service, 'getToken').mockReturnValue(null);
    expect(service.isAuthenticated()).toBe(false);
  });
});
