import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamRequestViewComponent } from './team-request-view.component';

describe('TeamRequestViewComponent', () => {
  let component: TeamRequestViewComponent;
  let fixture: ComponentFixture<TeamRequestViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TeamRequestViewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeamRequestViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
