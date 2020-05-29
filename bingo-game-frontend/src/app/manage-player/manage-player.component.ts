import { Component, OnInit, Input, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { BingoService } from '../bingo.service';
import { PlayerResponse } from '../models';

@Component({
  selector: 'app-manage-player',
  templateUrl: './manage-player.component.html',
  styleUrls: ['./manage-player.component.scss']
})
export class ManagePlayerComponent implements OnInit {

  @Input() gameId: string;
  @Input() gameStarted: boolean;
  @Input() playerSetupComplete: boolean;
  @ViewChild('fileInput') fileInput: ElementRef;
  @Output() isPlayerSetupReady = new EventEmitter<boolean>();

  players: any;
  dynamicForm: FormGroup;
  submitted = false;
  numberOfPLayers = 0;
  PlayerResponses: PlayerResponse[] = [];
  isAddPlayerFromExcel = false;
  isAddPlayerFromManually = true;

  constructor(private bingoService: BingoService, private elem: ElementRef, private formBuilder: FormBuilder) { }

  ngOnInit(): void {
    this.dynamicForm = this.formBuilder.group({
      players: new FormArray([])
    });

    if (this.playerSetupComplete) {
      this.bingoService.getBingoPlayers(this.gameId).subscribe((r) => {
        this.players = r;
        if (this.players.length > 0) {
          this.proceedGame();
        }
      });
    }
  }

  // convenience getters for easy access to form fields
  get f() { return this.dynamicForm.controls; }
  get t() { return this.f.players as FormArray; }

  proceedGame() {
    this.isPlayerSetupReady.emit(this.playerSetupComplete);
  }

  addPlayerFromExcel() {
    this.isAddPlayerFromExcel = true;
    this.isAddPlayerFromManually = false;
  }

  addPlayerManually() {
    this.isAddPlayerFromExcel = false;
    this.isAddPlayerFromManually = true;
  }

  addPlayer() {
    this.submitted = false;
    this.t.push(this.formBuilder.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]]
    }));
    this.numberOfPLayers++;
  }

  isValid() {
    return this.t.status === 'VALID';
  }

  removePlayer(playerNo: number) {
    this.submitted = false;
    this.t.removeAt(playerNo);
    this.numberOfPLayers--;
    return;
  }

  onReset() {
    // reset whole form back to initial state
    this.submitted = false;
    this.dynamicForm.reset();
    this.t.clear();
    this.numberOfPLayers = 0;
  }

  onClear() {
    // clear errors and reset ticket fields
    this.submitted = false;
    this.t.reset();
  }

  getSampleExcelLink() {
    this.bingoService.getSampleExcel().subscribe(
      blob => {
        const blobUrl = window.URL.createObjectURL(blob);
        window.open(blobUrl, '', 'left=20,top=20,width=700,height=600,toolbar=1,resizable=0');
      }
    );
  }

  uploadExcel() {
    const file = this.fileInput.nativeElement.files[0];

    if (!file) {
      return;
    }
    this.bingoService.uploadExcel(this.gameId, file).subscribe(
      res => {
        this.bingoService.getBingoPlayers(this.gameId).subscribe((r) => {
          this.players = r;
          this.playerSetupComplete = true;
          this.proceedGame();
        });
      });
  }

  onSubmit() {
    this.submitted = true;

    // stop here if form is invalid
    if (this.dynamicForm.invalid) {
      return;
    }

    this.t.value.forEach(element => {
      this.PlayerResponses.push(new PlayerResponse(element.name, element.email));
    });

    // display form values on success
    // alert('SUCCESS!! :-)\n\n' + JSON.stringify(this.dynamicForm.value, null, 4));

    this.bingoService.addPlayers(this.gameId, this.PlayerResponses).subscribe(
      res => {
        this.bingoService.getBingoPlayers(this.gameId).subscribe((r) => {
          this.players = r;
          this.playerSetupComplete = true;
          this.proceedGame();
        });
      });
  }

}
