import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AccountsService } from './accounts.service';
import { AuthService } from './auth.service';
import { Account } from '../store/accounts/accounts.actions';

describe('AccountsService', () => {
  let service: AccountsService;
  let httpMock: HttpTestingController;
  let authServiceMock: jest.Mocked<AuthService>;

  const mockToken = 'mock-jwt-token-123';
  const apiUrl = 'http://localhost:8080/api/accounts';

  beforeEach(() => {
    const spy = {
      getToken: jest.fn(),
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AccountsService, { provide: AuthService, useValue: spy }],
    });

    service = TestBed.inject(AccountsService);
    httpMock = TestBed.inject(HttpTestingController);
    authServiceMock = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    authServiceMock.getToken.mockReturnValue(mockToken);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get accounts with correct headers and url', () => {
    const mockAccounts: Account[] = [
      {
        uuid: 'uuid-123',
        iban: 'EE1234567890',
        currency: 'BRL',
        balance: 1000,
        user: {} as any,
      },
    ];

    service.getAccounts().subscribe((accounts) => {
      expect(accounts).toEqual(mockAccounts);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockAccounts);
  });

  it('should get account by iban with correct headers and url', () => {
    const iban = 'EE1234567890';
    const mockAccount: Account = {
      uuid: 'uuid-123',
      iban,
      currency: 'EUR',
      balance: 500,
      user: {} as any,
    };

    service.getAccountByIban(iban).subscribe((account) => {
      expect(account).toEqual(mockAccount);
    });

    const req = httpMock.expectOne(`${apiUrl}/${iban}`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockAccount);
  });

  it('should create account with correct body, headers and url', () => {
    const currency = 'USD';
    const mockAccount: Account = {
      uuid: 'uuid-456',
      iban: 'EE0987654321',
      currency,
      balance: 0,
      user: {} as any,
    };

    service.createAccount(currency).subscribe((account) => {
      expect(account).toEqual(mockAccount);
    });

    const req = httpMock.expectOne(`${apiUrl}/create`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ currency });
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockAccount);
  });
});
