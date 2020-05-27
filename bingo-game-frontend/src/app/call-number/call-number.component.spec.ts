import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CallNumberComponent } from './call-number.component';

describe('CallNumberComponent', () => {
  let component: CallNumberComponent;
  let fixture: ComponentFixture<CallNumberComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ CallNumberComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CallNumberComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
