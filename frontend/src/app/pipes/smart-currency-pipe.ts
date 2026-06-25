import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'smartCurrency',
  standalone: true,
})
export class SmartCurrencyPipe implements PipeTransform {
  transform(value: number | string | null | undefined): string {
    if (value === null || value === undefined || value === '') return '-';

    const num = typeof value === 'string' ? parseFloat(value) : value;

    if (isNaN(num)) return '-';

    if (num > 0 && num < 0.01) {
      return num.toString();
    }

    const truncated = this.truncateToDecimals(num, 2);

    return new Intl.NumberFormat('de-DE', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(truncated);
  }

  private truncateToDecimals(value: number, decimals: number): number {
    const factor = Math.pow(10, decimals);
    return Math.trunc(value * factor) / factor;
  }
}
