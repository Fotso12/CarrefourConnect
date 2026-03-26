import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GestionAbonnements } from './gestion-abonnements';

describe('GestionAbonnements', () => {
  let component: GestionAbonnements;
  let fixture: ComponentFixture<GestionAbonnements>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GestionAbonnements]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GestionAbonnements);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
