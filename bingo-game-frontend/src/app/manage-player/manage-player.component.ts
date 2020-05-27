import { Component, OnInit, Input, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { BingoService } from '../bingo.service';

class PlayerWrapper {
  name: string;
  email: string;
  id = 'id';

  constructor(theName: string, theEmail: string) {
    this.name = theName;
    this.email = theEmail;
  }
}

@Component({
  selector: 'app-manage-player',
  templateUrl: './manage-player.component.html',
  styleUrls: ['./manage-player.component.scss']
})
export class ManagePlayerComponent implements OnInit {

  @Input() gameId: string;
  @Input() gameStarted: boolean;
  @ViewChild('fileInput') fileInput: ElementRef;
  @Output() isPlayerSetupReady = new EventEmitter<boolean>();

  players: any;
  dynamicForm: FormGroup;
  submitted = false;
  numberOfPLayers = 0;
  playerWrappers: PlayerWrapper[] = [];
  isExcelImported = false;
  playersAddedManually = false;

  constructor(private bingoService: BingoService, private elem: ElementRef, private formBuilder: FormBuilder) {
  }

  ngOnInit(): void {
    this.dynamicForm = this.formBuilder.group({
      players: new FormArray([])
    });
  }

  // convenience getters for easy access to form fields
  get f() { return this.dynamicForm.controls; }
  get t() { return this.f.players as FormArray; }


  proceedGame() {
    this.isPlayerSetupReady.emit(true);
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

  onSubmit() {
    this.submitted = true;

    // stop here if form is invalid
    if (this.dynamicForm.invalid) {
      return;
    }

    this.t.value.forEach(element => {
      this.playerWrappers.push(new PlayerWrapper(element.name, element.email));
    });

    console.log(this.playerWrappers);
    // display form values on success
    // alert('SUCCESS!! :-)\n\n' + JSON.stringify(this.dynamicForm.value, null, 4));

    this.bingoService.addPlayers(this.gameId, this.playerWrappers).subscribe(
      res => {
        this.bingoService.getBingoPlayers(this.gameId).subscribe((r) => {
          this.players = r;
          this.playersAddedManually = true;
        });
      });
  }

  getSampleExcelLink() {
    this.bingoService.getSampleExcel().subscribe(
      blob => {
        const blobUrl = window.URL.createObjectURL(blob);
        window.open(blobUrl, '', 'left=20,top=20,width=600,height=600,toolbar=1,resizable=0');
      }
    );
  }

  getUserSlips(email) {
    this.bingoService.getUserSlips(this.gameId, email).subscribe((r) => {
      console.log(r);
    });
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
          this.isExcelImported = true;
        });
      });
  }

}
