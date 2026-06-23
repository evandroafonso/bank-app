import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { logout } from '../store/auth/auth.actions';
import { selectUser } from '../store/auth/auth.selectors';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './main-layout.component.html',
  styleUrls: ['./main-layout.component.scss'],
})
export class MainLayoutComponent {
  user$: Observable<any>;

  constructor(private store: Store) {
    this.user$ = this.store.select(selectUser);
  }

  onLogout(): void {
    this.store.dispatch(logout());
  }
}
