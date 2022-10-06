import { TestBed } from '@angular/core/testing';

import { AuthguardTeamLeadService } from './authguard-team-lead.service';

describe('AuthguardTeamLeadService', () => {
  let service: AuthguardTeamLeadService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthguardTeamLeadService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
