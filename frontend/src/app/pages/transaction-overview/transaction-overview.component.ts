import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-transaction-overview',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div style="padding: 40px; font-family: sans-serif;">
      <h1>Transaction Overview</h1>
      <a routerLink="/dashboard">Back</a>
    </div>
  `,
})
export class TransactionOverviewComponent {}
