import { TestBed } from '@angular/core/testing';

import { AuthentificationInterceptor } from './authentification.interceptor';

describe('AuthentificationService', () => {
  let service: AuthentificationInterceptor;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthentificationInterceptor);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
