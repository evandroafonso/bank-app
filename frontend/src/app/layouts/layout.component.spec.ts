import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { provideMockStore, MockStore } from '@ngrx/store/testing';
import { MainLayoutComponent } from './main-layout.component';
import { selectUser } from '../store/auth/auth.selectors';
import { logout } from '../store/auth/auth.actions';

describe('MainLayoutComponent', () => {
  let component: MainLayoutComponent;
  let fixture: ComponentFixture<MainLayoutComponent>;
  let store: MockStore;

  const mockUser = {
    id: '1',
    username: 'evandro',
    token: 'mock-token',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MainLayoutComponent, RouterTestingModule],
      providers: [
        provideMockStore({
          selectors: [{ selector: selectUser, value: mockUser }],
        }),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MainLayoutComponent);
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
