import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BingoService } from '../bingo.service';
import { Subscription, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-game-room',
  templateUrl: './game-room.component.html',
  styleUrls: ['./game-room.component.scss']
})
export class GameRoomComponent implements OnInit {

  gameId;
  callsDone;
  calls: any = [];
  subscription: Subscription;
  statusText: string;

  constructor(private route: ActivatedRoute, public bingoService: BingoService) {

    this.route.url.subscribe((res: any) => {
      this.gameId = res[1].path;
    });

    this.subscription = timer(0, 3000).pipe(
      switchMap(() => this.bingoService.getAllCalls(this.gameId))
    ).subscribe(callsDone => {
      this.callsDone = callsDone;
      this.calls = [];
      for (const val of Object.values(this.callsDone)) {
        this.calls.push(val);
      }

    });

  }

  ngOnInit(): void {
  }

}
