import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LandingPageAdminComponent } from './landing-page-admin.component';

describe('LandingPageAdminComponent', () => {
  let component: LandingPageAdminComponent;
  let fixture: ComponentFixture<LandingPageAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LandingPageAdminComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LandingPageAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
