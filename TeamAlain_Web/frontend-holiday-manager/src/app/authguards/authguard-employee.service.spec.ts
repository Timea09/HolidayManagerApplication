import { TestBed } from '@angular/core/testing';

import { AuthguardEmployeeService } from './authguard-employee.service';

describe('AuthguardEmployeeService', () => {
  let service: AuthguardEmployeeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthguardEmployeeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
