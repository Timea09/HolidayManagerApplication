import { TestBed } from '@angular/core/testing';

import { AuthguardAdminService } from './authguard-admin.service';

describe('AuthguardAdminService', () => {
  let service: AuthguardAdminService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthguardAdminService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
