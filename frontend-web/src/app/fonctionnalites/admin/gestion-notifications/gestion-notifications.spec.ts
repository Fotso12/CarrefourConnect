import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionNotifications } from './gestion-notifications';

describe('GestionNotifications', () => {
  let component: GestionNotifications;
  let fixture: ComponentFixture<GestionNotifications>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionNotifications]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionNotifications);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
