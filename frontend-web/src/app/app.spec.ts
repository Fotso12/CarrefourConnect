import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from './coeur/services/auth.service';

describe('App', () => {
  let mockAuthService: any;

  beforeEach(async () => {
    mockAuthService = {
      currentUser: of({ role: 'ROLE_USER' }),
      currentUserValue: { role: 'ROLE_USER' },
      isLoggedIn: () => true,
      logout: () => {}
    };

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render title', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    // Le titre est maintenant 'CarrefourConnect' dans la classe App
    expect(compiled.querySelector('span')?.textContent).toContain('Carrefour');
  });
});
