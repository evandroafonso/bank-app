import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { SidebarComponent } from './sidebar.component';
import { selectUser } from '../../store/auth/auth.selectors';
import { logout } from '../../store/auth/auth.actions';
import { AuthUser } from '../../store/auth/auth.actions';

describe('SidebarComponent', () => {
  let component: SidebarComponent;
  let fixture: ComponentFixture<SidebarComponent>;
  let store: MockStore;

  const mockUser: AuthUser = {
    id: '1',
    username: 'evandro',
    token: 'mock-token',
  } as any;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarComponent, RouterTestingModule],
      providers: [
        provideMockStore({
          selectors: [{ selector: selectUser, value: mockUser }],
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SidebarComponent);
    component = fixture.componentInstance;
    store = TestBed.inject(MockStore);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select user from store on initialization', (done) => {
    component.user$.subscribe((user) => {
      expect(user).toEqual(mockUser);
      done();
    });
  });

  it('should dispatch logout action when onLogout is called', () => {
    const dispatchSpy = jest.spyOn(store, 'dispatch');

    component.onLogout();

    expect(dispatchSpy).toHaveBeenCalledWith(logout());
  });
});
