import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PlayerResponse } from '../models';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { BingoService } from '../bingo.service';

@Component({
  selector: 'app-setup',
  templateUrl: './setup.component.html',
  styleUrls: ['./setup.component.scss']
})
export class SetupComponent implements OnInit {

  gameId;
  shouldStartGame = false;
  leaderAssigned = false;
  isExcelUploaded = false;
  playerSetupComplete = false;
  bingoBoardReady = false;
  leader: PlayerResponse;
  callsStarted = false;
  callsDone;
  slipsNeeded = 6;
  boardType = 'GAMEBOARD_90';

  leaderForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.pattern('^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$')]),
    name: new FormControl('', [
      Validators.required
    ])
  });

  constructor(private route: ActivatedRoute, public bingoService: BingoService) {

    this.route.data.subscribe((res) => {
      const r = res.gameSetupStatus;
      this.gameId = r.gameId;
      this.leaderAssigned = r.leaderAssigned;
      this.isExcelUploaded = r.excelUploaded;
      this.playerSetupComplete = r.playerSetupComplete;
      this.callsStarted = r.haveCallsStarted;
      this.bingoBoardReady = r.bingoBoardReady;

      this.bingoService.getAllCalls(this.gameId).subscribe((callsDone: any) => {
        this.callsDone = callsDone;
      });
    });
  }

  ngOnInit(): void {
  }

  getPlayerSetupStatus(startCall: boolean) {
    this.shouldStartGame = startCall;
  }

  proceedWithCalls() {
    this.bingoService.getAllCalls(this.gameId).subscribe((callsDone: any) => {
      this.callsDone = callsDone;
      this.callsStarted = true;
    });
  }

  onSlipSelectedChange(value: number) {
    this.slipsNeeded = value;
  }

  onBoardTypeSelectedChange(value: string) {
    this.boardType = value;
  }

  sendEmail() {
    this.bingoService.sendEmail(this.gameId).subscribe((r) => {
      this.callsStarted = true;
    });
  }

  onSubmit() {
    this.leader = new PlayerResponse(this.leaderForm.value.name, this.leaderForm.value.email);
    this.bingoService.assignLeader(this.gameId, this.leader).subscribe((r) => {
      this.leaderAssigned = true;
      this.bingoService.setLeader(r);
    });
  }

  setUpBoardTypeAndSlipCount() {
    this.bingoService.setUpBoardTypeAndSlipCount(this.gameId, this.boardType, this.slipsNeeded).subscribe((r) => {
      this.bingoBoardReady = true;
    });
  }

}
