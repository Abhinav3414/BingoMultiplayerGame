import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BingoService } from '../bingo.service';

@Component({
  selector: 'app-setup',
  templateUrl: './setup.component.html',
  styleUrls: ['./setup.component.scss']
})
export class SetupComponent implements OnInit {

  gameId;
  leaderAssigned = false;
  isExcelUploaded = false;
  playerSetupComplete = false;
  bingoBoardReady = false;
  callsStarted = false;
  callsDone;
  slipsNeeded = 6;
  boardType = 'GAMEBOARD_90';
  newGame = true;
  existingGameId;
  notAuthorized = false;
  emailSlips = false;
  bingoSlipEmailStatus = 'DISABLED';

  constructor(private route: ActivatedRoute, public bingoService: BingoService, private router: Router) {

    this.route.data.subscribe((res) => {
      const r = res.gameSetupStatus;
      this.gameId = r.gameId;
      this.leaderAssigned = r.leaderAssigned;
      this.isExcelUploaded = r.excelUploaded;
      this.playerSetupComplete = r.playerSetupComplete;
      this.callsStarted = r.haveCallsStarted;
      this.bingoBoardReady = r.bingoBoardReady;
      this.bingoSlipEmailStatus = r.bingoSlipEmailStatus;
      if (r.haveCallsStarted) {
        this.bingoService.getAllCalls(this.gameId).subscribe((callsDone: any) => {
          this.callsDone = callsDone;
        });
      }
    });
  }

  ngOnInit(): void {
  }


  initiateNewGame() {
    this.newGame = true;
  }

  existingGame() {
    this.newGame = false;
  }

  getPlayerSetupStatus(startCall: boolean) {
    this.playerSetupComplete = startCall;
  }

  getLeaderAssignStatus(leaderIsAssigned: boolean) {
    console.log(leaderIsAssigned);
    this.leaderAssigned = leaderIsAssigned;
  }
  proceedWithCalls() {
    this.bingoService.startCalls(this.gameId).subscribe(() => {
      this.callsDone = {};
      this.callsStarted = true;
    });
  }

  onSlipSelectedChange(value: number) {
    this.slipsNeeded = value;
  }

  onBoardTypeSelectedChange(value: string) {
    this.boardType = value;
  }

  setUpBoardTypeAndSlipCount() {
    if (this.emailSlips) {
      this.bingoSlipEmailStatus = 'NOT_SENT';
    }
    this.bingoService.setUpBoardTypeAndSlipCount(this.gameId, this.boardType, this.slipsNeeded, this.emailSlips).subscribe((r) => {
      this.bingoBoardReady = true;
    });
  }

  enterExistingGame() {
    this.bingoService.enterGameRoom(this.existingGameId).subscribe((r) => {
      this.bingoService.setLeader(r);
      this.gameId = this.existingGameId;
      this.bingoBoardReady = true;
      this.router.navigate(['game', this.existingGameId]).then(() => {
        window.location.reload();
      });
    }, (err) => {
      console.log('not authorized');
      this.notAuthorized = true;
    });
  }

}
