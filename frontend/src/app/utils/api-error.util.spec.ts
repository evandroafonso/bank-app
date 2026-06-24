import {
  getApiErrorMessage,
  extractBackendErrorMessage,
  BackendErrorResponse,
} from './api-error.util';

describe('ApiError Utility', () => {
  const fallbackMessage = 'An unexpected error occurred.';

  describe('getApiErrorMessage', () => {
    it('should return extracted error message when extraction succeeds', () => {
      const errorObj = { error: { message: 'Specific API Error' } };
      const result = getApiErrorMessage(errorObj, fallbackMessage);
      expect(result).toBe('Specific API Error');
    });

    it('should return fallback message when extraction returns null', () => {
      const errorObj = null;
      const result = getApiErrorMessage(errorObj, fallbackMessage);
      expect(result).toBe(fallbackMessage);
    });
  });

  describe('extractBackendErrorMessage', () => {
    it('should return null if error is null or undefined', () => {
      expect(extractBackendErrorMessage(null)).toBeNull();
      expect(extractBackendErrorMessage(undefined)).toBeNull();
    });

    it('should return null if error is not an object', () => {
      expect(extractBackendErrorMessage('String Error')).toBeNull();
      expect(extractBackendErrorMessage(500)).toBeNull();
    });

    it('should return trimmed string if body is a plain string', () => {
      const errorObj = { error: '  Plain error text  ' };
      expect(extractBackendErrorMessage(errorObj)).toBe('Plain error text');
    });

    it('should extract message from JSON string body if it matches BackendErrorResponse', () => {
      const backendError: BackendErrorResponse = {
        message: 'Error from stringified JSON',
        errorCode: 'ERR001',
        status: 400,
        timestamp: '2026-06-24T00:00:00Z',
      };
      const errorObj = { error: JSON.stringify(backendError) };
      expect(extractBackendErrorMessage(errorObj)).toBe('Error from stringified JSON');
    });

    it('should extract message from generic JSON string object', () => {
      const errorObj = { error: JSON.stringify({ message: 'Generic JSON Message' }) };
      expect(extractBackendErrorMessage(errorObj)).toBe('Generic JSON Message');
    });

    it('should return the original string if JSON parsing fails', () => {
      const errorObj = { error: 'Invalid { JSON string' };
      expect(extractBackendErrorMessage(errorObj)).toBe('Invalid { JSON string');
    });

    it('should extract message if body object matches BackendErrorResponse directly', () => {
      const backendError: BackendErrorResponse = {
        message: 'Direct Backend Error',
        errorCode: 'ERR002',
        status: 500,
        timestamp: '2026-06-24T00:00:00Z',
      };
      const errorObj = { error: backendError };
      expect(extractBackendErrorMessage(errorObj)).toBe('Direct Backend Error');
    });

    it('should extract message from body object', () => {
      const errorObj = { error: { message: '  Body Message  ' } };
      expect(extractBackendErrorMessage(errorObj)).toBe('Body Message');
    });

    it('should extract error from body object if message is absent', () => {
      const errorObj = { error: { error: '  Body Error Property  ' } };
      expect(extractBackendErrorMessage(errorObj)).toBe('Body Error Property');
    });

    it('should extract detail from body object if message and error are absent', () => {
      const errorObj = { error: { detail: '  Body Detail Property  ' } };
      expect(extractBackendErrorMessage(errorObj)).toBe('Body Detail Property');
    });

    it('should extract message from top-level response object if body properties are absent', () => {
      const errorObj = { message: '  Top level response message  ', error: {} };
      expect(extractBackendErrorMessage(errorObj)).toBe('Top level response message');
    });

    it('should return null if body is a primitive non-string or empty object with no matching properties', () => {
      expect(extractBackendErrorMessage({ error: 12345 })).toBeNull();
      expect(extractBackendErrorMessage({ error: {} })).toBeNull();
    });
  });
});
