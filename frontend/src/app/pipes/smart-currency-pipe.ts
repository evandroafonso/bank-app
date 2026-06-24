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

    return new Intl.NumberFormat('en-US', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(num);
  }
}
