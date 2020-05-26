import { TestBed } from '@angular/core/testing';

import { BingoService } from './bingo.service';

describe('BingoServiceService', () => {
  let service: BingoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BingoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
