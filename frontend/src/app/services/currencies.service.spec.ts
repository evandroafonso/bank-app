import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CurrenciesService, Currency } from './currencies.service';
import { AuthService } from './auth.service';

describe('CurrenciesService', () => {
  let service: CurrenciesService;
  let httpMock: HttpTestingController;
  let authServiceMock: jest.Mocked<AuthService>;

  const mockToken = 'mock-jwt-token-123';
  const apiUrl = 'http://localhost:8080/api/currencies';

  beforeEach(() => {
    const spy = {
      getToken: jest.fn(),
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CurrenciesService, { provide: AuthService, useValue: spy }],
    });

    service = TestBed.inject(CurrenciesService);
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

  it('should get currencies with correct headers and url', () => {
    const mockCurrencies: Currency[] = [
      { code: 'EUR', description: 'Euro' },
      { code: 'USD', description: 'US Dollar' },
    ];

    service.getCurrencies().subscribe((currencies) => {
      expect(currencies).toEqual(mockCurrencies);
    });

    const req = httpMock.expectOne(apiUrl);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockCurrencies);
  });
});
