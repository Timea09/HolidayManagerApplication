import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TeamRequestsListComponent } from './team-requests-list.component';

describe('TeamRequestsListComponent', () => {
  let component: TeamRequestsListComponent;
  let fixture: ComponentFixture<TeamRequestsListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TeamRequestsListComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TeamRequestsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
