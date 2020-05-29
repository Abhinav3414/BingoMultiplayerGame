import { Component, OnInit, Input } from '@angular/core';
import { BingoService } from '../bingo.service';

@Component({
  selector: 'app-call-number',
  templateUrl: './call-number.component.html',
  styleUrls: ['./call-number.component.scss']
})
export class CallNumberComponent implements OnInit {
  @Input() gameId: string;
  calls: any = [];
  currentCall: any;

  constructor(private bingoService: BingoService) { }

  ngOnInit(): void {
  }

  callNext() {
    if (this.calls.length === 90) {
      return;
    }
    this.bingoService.callNext(this.gameId).subscribe(
      res => {
        this.calls.push(res);
        this.currentCall = res;
      });
  }

}
