import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TransactionsService, TransactionOperationRequest } from './transactions.service';
import { AuthService } from './auth.service';
import { Transaction, TransactionPage } from '../store/transactions/transactions.actions';

describe('TransactionsService', () => {
  let service: TransactionsService;
  let httpMock: HttpTestingController;
  let authServiceMock: jest.Mocked<AuthService>;

  const mockToken = 'mock-jwt-token-123';
  const apiUrl = 'http://localhost:8080/api/transactions';

  beforeEach(() => {
    const spy = {
      getToken: jest.fn(),
    };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TransactionsService, { provide: AuthService, useValue: spy }],
    });

    service = TestBed.inject(TransactionsService);
    httpMock = TestBed.inject(HttpTestingController);
    authServiceMock = TestBed.inject(AuthService) as jest.Mocked<AuthService>;

    authServiceMock.getToken.mockReturnValue(mockToken);
    jest.spyOn(Storage.prototype, 'getItem').mockReturnValue(mockToken);
  });

  afterEach(() => {
    httpMock.verify();
    jest.clearAllMocks();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get balance chart data with correct params and headers', () => {
    const iban = 'EE1234567890';
    const startDate = '2026-01-01';
    const endDate = '2026-01-31';
    const mockChartData = [{ timestamp: '2026-01-15T10:00:00Z', balance: 1500 }];

    service.getBalanceChart(iban, startDate, endDate).subscribe((data) => {
      expect(data).toEqual(mockChartData);
    });

    const req = httpMock.expectOne(
      (request) =>
        request.url === `${apiUrl}/balance-chart` &&
        request.params.get('iban') === iban &&
        request.params.get('startDate') === startDate &&
        request.params.get('endDate') === endDate,
    );

    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockChartData);
  });

  it('should get transaction history with correct pagination params', () => {
    const iban = 'EE1234567890';
    const page = 0;
    const size = 10;
    const mockHistory: TransactionPage = { content: [], totalElements: 0 } as any;

    service.getTransactionHistory(iban, page, size).subscribe((history) => {
      expect(history).toEqual(mockHistory);
    });

    const req = httpMock.expectOne(
      (request) =>
        request.url === `${apiUrl}/history/${iban}` &&
        request.params.get('page') === page.toString() &&
        request.params.get('size') === size.toString(),
    );

    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockHistory);
  });

  it('should get transaction by uuid', () => {
    const uuid = 'uuid-transaction-999';
    const mockTransaction: Transaction = { uuid, amount: 250 } as any;

    service.getTransactionByUuid(uuid).subscribe((transaction) => {
      expect(transaction).toEqual(mockTransaction);
    });

    const req = httpMock.expectOne(`${apiUrl}/${uuid}`);
    expect(req.request.method).toBe('GET');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockTransaction);
  });

  it('should send credit operation payload', () => {
    const payload: TransactionOperationRequest = {
      iban: 'EE1234567890',
      amount: 500,
      description: 'Salary',
      currency: 'EUR',
    };
    const mockTransaction: Transaction = { uuid: 'uuid-1', amount: 500 } as any;

    service.creditAccount(payload).subscribe((transaction) => {
      expect(transaction).toEqual(mockTransaction);
    });

    const req = httpMock.expectOne(`${apiUrl}/credit`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockTransaction);
  });

  it('should send debit operation payload', () => {
    const payload: TransactionOperationRequest = {
      iban: 'EE1234567890',
      amount: 50,
      description: 'Supermarket',
      currency: 'EUR',
    };
    const mockTransaction: Transaction = { uuid: 'uuid-2', amount: -50 } as any;

    service.debitAccount(payload).subscribe((transaction) => {
      expect(transaction).toEqual(mockTransaction);
    });

    const req = httpMock.expectOne(`${apiUrl}/debit`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(payload);
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    req.flush(mockTransaction);
  });

  it('should export transaction pdf and handle blob response', () => {
    const uuid = 'uuid-pdf-123';
    const mockBlob = new Blob(['pdf-content'], { type: 'application/pdf' });

    service.exportTransactionPdf(uuid).subscribe((response) => {
      expect(response).toEqual(mockBlob);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/reports/transaction/pdf');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ transactionUuid: uuid });
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
    expect(req.request.responseType).toBe('blob');
    req.flush(mockBlob);
  });
});
