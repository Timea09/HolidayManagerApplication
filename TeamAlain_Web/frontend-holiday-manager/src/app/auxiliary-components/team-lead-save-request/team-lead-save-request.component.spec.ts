import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamLeadSaveRequestComponent } from './team-lead-save-request.component';

describe('TeamLeadSaveRequestComponent', () => {
  let component: TeamLeadSaveRequestComponent;
  let fixture: ComponentFixture<TeamLeadSaveRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TeamLeadSaveRequestComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeamLeadSaveRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
