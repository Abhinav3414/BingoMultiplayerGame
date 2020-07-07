import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { BingoService } from '../bingo.service';
import { Subscription, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { ReturnStatement } from '@angular/compiler';
import { PlayerResponse } from '../models';

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

  playeruniqueid;
  slipResponse;
  lastCall;
  is75Board = false;
  joinGameViaLink = false;

  playerName;
  playerEmail;

  constructor(private route: ActivatedRoute, public bingoService: BingoService) {

    this.route.url.subscribe((res: any) => {
      this.gameId = res[1].path;
    });

    this.route.data.subscribe((res) => {
      if (res.gameSetupStatus) {
        const r = res.gameSetupStatus;
        this.joinGameViaLink = r.joinGameViaLink;

        if (r.gameName && !this.bingoService.getGameName()) {
          this.bingoService.setGameName(r.gameName);
        }
      }
    });

    this.subscription = timer(0, 3000).pipe(switchMap(() => this.bingoService.getAllCalls(this.gameId)))
      .subscribe(callsDone => {
        this.callsDone = callsDone;
        this.calls = [];
        for (const val of Object.values(this.callsDone)) {
          this.calls.push(val);
          this.lastCall = val;
        }
      });

  }

  ngOnInit(): void {
  }

  viewSlips() {
    this.bingoService.getUserSlips(this.gameId, this.playeruniqueid).subscribe((res) => {
      this.slipResponse = res;
      if (this.slipResponse.responses[0].transformedMatrix[0].length === 5) {
        this.is75Board = true;
      }
    });
  }

  clickOnNumber(row: number, col: number, slipId: number) {
    this.bingoService.updateSlipNumber(this.gameId, this.playeruniqueid, row, col, slipId).subscribe((slipRes) => {
      this.slipResponse.responses.forEach(sr => {
        if (sr.slipId === slipRes.slipId) {
          sr.transformedMatrix = slipRes.transformedMatrix;
        }
      });
    });
  }

  joinPlayer() {
    if (this.playerName && this.playerEmail) {
      const playerResponse = new PlayerResponse(this.playerName, this.playerEmail);
      this.bingoService.joinPlayer(this.gameId, playerResponse).subscribe((res: any) => {

        this.bingoService.getUserSlips(this.gameId, res.id).subscribe((r) => {
          this.slipResponse = r;
          if (this.slipResponse.responses[0].transformedMatrix[0].length === 5) {
            this.is75Board = true;
          }
        });


      });
    }
  }

}
