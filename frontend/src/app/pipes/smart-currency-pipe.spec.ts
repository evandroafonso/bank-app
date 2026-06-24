import { SmartCurrencyPipe } from './smart-currency-pipe';

describe('SmartCurrencyPipe', () => {
  let pipe: SmartCurrencyPipe;

  beforeEach(() => {
    pipe = new SmartCurrencyPipe();
  });

  it('should be created', () => {
    expect(pipe).toBeTruthy();
  });

  it('should return "-" when value is null, undefined, or empty string', () => {
    expect(pipe.transform(null)).toBe('-');
    expect(pipe.transform(undefined)).toBe('-');
    expect(pipe.transform('')).toBe('-');
  });

  it('should return "-" when value is an invalid string number', () => {
    expect(pipe.transform('abc')).toBe('-');
    expect(pipe.transform('123abc')).toBe('123.00');
    expect(pipe.transform('abc123')).toBe('-');
  });

  it('should return the raw string representation for values strictly between 0 and 0.01', () => {
    expect(pipe.transform(0.005)).toBe('0.005');
    expect(pipe.transform('0.0001')).toBe('0.0001');
  });

  it('should format standard numbers to en-US style with two decimal places', () => {
    expect(pipe.transform(1000)).toBe('1,000.00');
    expect(pipe.transform(1234.567)).toBe('1,234.57');
    expect(pipe.transform(0.5)).toBe('0.50');
    expect(pipe.transform(0)).toBe('0.00');
    expect(pipe.transform(-150.5)).toBe('-150.50');
  });

  it('should correctly parse and format numeric strings', () => {
    expect(pipe.transform('1500.75')).toBe('1,500.75');
    expect(pipe.transform('-50')).toBe('-50.00');
  });
});
