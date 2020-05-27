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
  leader: PlayerResponse;

  leaderForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.pattern('^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$')]),
    name: new FormControl('', [
      Validators.required
    ])
  });

  constructor(private route: ActivatedRoute, private bingoService: BingoService) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.gameId = params.get('gameId');
      this.bingoService.getGameSetupStatus(this.gameId).subscribe((r: any) => {
        this.leaderAssigned = r.leaderAssigned;
        this.isExcelUploaded = r.excelUploaded;
        this.playerSetupComplete = r.playerSetupComplete;
      });
    });
  }

  getPlayerSetupStatus(startCall: boolean) {
    this.shouldStartGame = startCall;
  }

  onSubmit() {
    this.leader = new PlayerResponse(this.leaderForm.value.name, this.leaderForm.value.email);
    this.bingoService.assignLeader(this.gameId, this.leader).subscribe((r) => {
      this.leaderAssigned = true;
    });
  }

}
