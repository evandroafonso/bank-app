import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { of, throwError } from 'rxjs';

import { TransactionOverviewComponent } from './transaction-overview.component';
import { TransactionsService } from '../../services/transactions.service';
import { SmartCurrencyPipe } from '../../pipes/smart-currency-pipe';
import {
  clearTransactionDetail,
  loadTransactionDetail,
} from '../../store/transaction-detail/transaction-detail.actions';
import {
  selectTransactionDetail,
  selectTransactionDetailError,
  selectTransactionDetailLoading,
} from '../../store/transaction-detail/transaction-detail.selectors';
import { Transaction } from '../../store/transactions/transactions.actions';

const TEST_UUID = 'tx-123-abc';

const mockTransaction: Transaction = {
  uuid: TEST_UUID,
  sourceAmount: 250.0,
  currency: 'EUR',
  description: 'Payment for services',
  timestamp: '2026-06-24T14:30:00Z',
  type: 'DEBIT',
  convertedAmount: 250.0,
  exchangeRate: 1.0,
  targetCurrency: 'EUR',
  balance: 1250.50
};

@Component({
  selector: 'app-sidebar',
  template: '',
  standalone: true,
})
class SidebarStubComponent {}

describe('TransactionOverviewComponent', () => {
  let component: TransactionOverviewComponent;
  let fixture: ComponentFixture<TransactionOverviewComponent>;
  let store: MockStore;
  let dispatchSpy: jest.SpyInstance;
  let locationMock: jest.Mocked<Location>;
  let transactionsServiceMock: { exportTransactionPdf: jest.Mock };

  beforeEach(async () => {
    transactionsServiceMock = {
      exportTransactionPdf: jest.fn().mockReturnValue(of(new Blob(['pdf-content'], { type: 'application/pdf' }))),
    };

    locationMock = {
      back: jest.fn(),
    } as unknown as jest.Mocked<Location>;

    window.URL.createObjectURL = jest.fn().mockReturnValue('blob:mock-url');
    window.URL.revokeObjectURL = jest.fn();

    await TestBed.configureTestingModule({
      imports: [TransactionOverviewComponent],
      providers: [
        provideMockStore({
          selectors: [
            { selector: selectTransactionDetail, value: null },
            { selector: selectTransactionDetailLoading, value: false },
            { selector: selectTransactionDetailError, value: null },
          ],
        }),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: (key: string) => (key === 'uuid' ? TEST_UUID : null),
              },
            },
          },
        },
        { provide: Location, useValue: locationMock },
        { provide: TransactionsService, useValue: transactionsServiceMock },
      ],
    })
      .overrideComponent(TransactionOverviewComponent, {
        set: {
          imports: [CommonModule, SmartCurrencyPipe, SidebarStubComponent],
        },
      })
      .compileComponents();

    store = TestBed.inject(MockStore);
    dispatchSpy = jest.spyOn(store, 'dispatch');

    fixture = TestBed.createComponent(TransactionOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    jest.restoreAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should extract uuid from route and dispatch clear and load actions', () => {
      expect(dispatchSpy).toHaveBeenCalledWith(clearTransactionDetail());
      expect(dispatchSpy).toHaveBeenCalledWith(loadTransactionDetail({ uuid: TEST_UUID }));
    });

    it('should select data from store selectors', fakeAsync(() => {
      store.overrideSelector(selectTransactionDetail, mockTransaction);
      store.overrideSelector(selectTransactionDetailLoading, true);
      store.overrideSelector(selectTransactionDetailError, 'Mock Error');
      store.refreshState();

      component.transaction$.subscribe((tx) => expect(tx).toEqual(mockTransaction));
      component.loading$.subscribe((loading) => expect(loading).toBe(true));
      component.error$.subscribe((err) => expect(err).toBe('Mock Error'));
      tick();
    }));
  });

  describe('ngOnDestroy', () => {
    it('should complete destroy$ subject and dispatch clear action', () => {
      const nextSpy = jest.spyOn((component as any).destroy$, 'next');
      const completeSpy = jest.spyOn((component as any).destroy$, 'complete');

      component.ngOnDestroy();

      expect(nextSpy).toHaveBeenCalled();
      expect(completeSpy).toHaveBeenCalled();
      expect(dispatchSpy).toHaveBeenCalledWith(clearTransactionDetail());
    });
  });

  describe('goBack', () => {
    it('should call location.back to navigate to the previous page', () => {
      component.goBack();
      expect(locationMock.back).toHaveBeenCalled();
    });
  });

  describe('formatting helpers', () => {
    it('formatDate should format timestamp in en-US locale format', () => {
      const timestamp = '2026-06-24T14:30:00';
      const formatted = component.formatDate(timestamp);

      expect(formatted).toContain('2026');
      expect(formatted).toContain('Jun');
      expect(formatted).toContain('24');
    });

    it('hasCurrencyConversion should return true if currencies are different', () => {
      expect(component.hasCurrencyConversion('EUR', 'USD')).toBe(true);
    });

    it('hasCurrencyConversion should return false if currencies are identical', () => {
      expect(component.hasCurrencyConversion('BRL', 'BRL')).toBe(false);
    });
  });

  describe('downloadPdf', () => {
    let anchorMock: any;

    beforeEach(() => {
      anchorMock = {
        href: '',
        download: '',
        click: jest.fn(),
      };
      jest.spyOn(document, 'createElement').mockReturnValue(anchorMock);
    });

    it('should download transaction PDF successfully via DOM anchor manipulation', () => {
      component.downloadPdf();

      expect(transactionsServiceMock.exportTransactionPdf).toHaveBeenCalledWith(TEST_UUID);
      expect(window.URL.createObjectURL).toHaveBeenCalled();
      expect(anchorMock.download).toBe(`transaction_${TEST_UUID}.pdf`);
      expect(anchorMock.click).toHaveBeenCalled();
      expect(window.URL.revokeObjectURL).toHaveBeenCalledWith('blob:mock-url');
    });

    it('should catch and log error on console when API fails', () => {
      const consoleSpy = jest.spyOn(console, 'error').mockImplementation(() => {});
      transactionsServiceMock.exportTransactionPdf.mockReturnValue(throwError(() => new Error('API Error')));

      component.downloadPdf();

      expect(consoleSpy).toHaveBeenCalledWith('Erro ao baixar PDF:', expect.any(Error));
    });
  });
});