export interface BackendErrorResponse {
  message: string;
  errorCode: string;
  status: number;
  timestamp: string;
}

export function getApiErrorMessage(error: unknown, fallbackMessage: string): string {
  return extractBackendErrorMessage(error) ?? fallbackMessage;
}

export function extractBackendErrorMessage(error: unknown): string | null {
  if (!error || typeof error !== 'object') {
    return null;
  }

  const response = error as {
    error?: unknown;
    message?: unknown;
  };

  const body = response.error;

  if (isNonEmptyString(body)) {
    return extractMessageFromString(body);
  }

  if (isBackendErrorResponse(body)) {
    return body.message.trim();
  }

  if (!body || typeof body !== 'object') {
    return null;
  }

  const bodyWithMessage = body as {
    message?: unknown;
    error?: unknown;
    detail?: unknown;
  };

  if (isNonEmptyString(bodyWithMessage.message)) {
    return bodyWithMessage.message.trim();
  }

  if (isNonEmptyString(bodyWithMessage.error)) {
    return bodyWithMessage.error.trim();
  }

  if (isNonEmptyString(bodyWithMessage.detail)) {
    return bodyWithMessage.detail.trim();
  }

  if (isNonEmptyString(response.message)) {
    return response.message.trim();
  }

  return null;
}

function extractMessageFromString(value: string): string {
  const trimmedValue = value.trim();

  try {
    const parsedValue: unknown = JSON.parse(trimmedValue);

    if (isBackendErrorResponse(parsedValue)) {
      return parsedValue.message.trim();
    }

    if (parsedValue && typeof parsedValue === 'object') {
      const bodyWithMessage = parsedValue as { message?: unknown };

      if (isNonEmptyString(bodyWithMessage.message)) {
        return bodyWithMessage.message.trim();
      }
    }
  } catch {
    return trimmedValue;
  }

  return trimmedValue;
}

function isBackendErrorResponse(value: unknown): value is BackendErrorResponse {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const body = value as Partial<BackendErrorResponse>;

  return (
    isNonEmptyString(body.message) &&
    isNonEmptyString(body.errorCode) &&
    typeof body.status === 'number' &&
    isNonEmptyString(body.timestamp)
  );
}

function isNonEmptyString(value: unknown): value is string {
  return typeof value === 'string' && value.trim().length > 0;
}
