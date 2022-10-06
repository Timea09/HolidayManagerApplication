import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeSaveRequestComponent } from './employee-save-request.component';

describe('EmployeeSaveRequestComponent', () => {
  let component: EmployeeSaveRequestComponent;
  let fixture: ComponentFixture<EmployeeSaveRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmployeeSaveRequestComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeeSaveRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
