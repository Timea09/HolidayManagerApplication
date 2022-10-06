import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamLeadPageComponent } from './team-lead-page.component';

describe('TeamLeadPageComponent', () => {
  let component: TeamLeadPageComponent;
  let fixture: ComponentFixture<TeamLeadPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TeamLeadPageComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeamLeadPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
