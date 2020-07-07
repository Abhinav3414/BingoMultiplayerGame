import { Component, OnInit, Input, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { BingoService } from '../bingo.service';
import { PlayerResponse } from '../models';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-manage-player',
  templateUrl: './manage-player.component.html',
  styleUrls: ['./manage-player.component.scss']
})
export class ManagePlayerComponent implements OnInit {

  constructor(private bingoService: BingoService, private elem: ElementRef, private formBuilder: FormBuilder,
    private modalService: NgbModal) {
    this.hrefUrl = window.location.href.substr(0, window.location.href.indexOf('#') + 2);
  }

  // convenience getters for easy access to form fields
  get f() { return this.dynamicForm.controls; }
  get t() { return this.f.players as FormArray; }

  @Input() gameId: string;
  @Input() playerSetupComplete: boolean;
  @Input() bingoSlipEmailStatus: string;
  @Input() joinGameViaLink: boolean;
  @ViewChild('fileInput') fileInput: ElementRef;
  @Output() isPlayerSetupReady = new EventEmitter<boolean>();

  players: any;
  dynamicForm: FormGroup;
  submitted = false;
  numberOfPLayers = 0;
  PlayerResponses: PlayerResponse[] = [];
  isAddPlayerFromExcel = false;
  isAddPlayerFromManually = true;
  hrefUrl;

  emailSendPlayerIdentifier;
  fetching = false;

  imageBlobUrl;
  closeResult: string;

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

    let formBuilderGroup;
    if (this.bingoSlipEmailStatus === 'DISABLED') {
      formBuilderGroup = this.formBuilder.group({
        name: ['', Validators.required]
      });

    } else {
      formBuilderGroup = this.formBuilder.group({
        name: ['', Validators.required],
        email: ['', [Validators.required, Validators.email]]
      });
    }

    this.t.push(formBuilderGroup);
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

  createImageFromBlob() {
    this.bingoService.getSampleExcel().subscribe(
      blob => {
        const reader = new FileReader();
        reader.addEventListener('load', () => {
          this.imageBlobUrl = reader.result;
        }, false);
        if (blob) {
          reader.readAsDataURL(blob);
        }
      }
    );
  }

  open(content) {

    this.createImageFromBlob();

    this.modalService.open(content,
      { ariaLabelledBy: 'modal-basic-title', centered: true, size: 'lg', scrollable: true }).result.then((result) => {
        this.closeResult = `Closed with: ${result}`;
      }, (reason) => {
        this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
      });
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
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

  sendEmailToAll() {
    this.fetching = true;
    this.bingoService.sendEmailToAll(this.gameId).subscribe((r) => {
      this.bingoSlipEmailStatus = 'SENT';

      this.bingoService.getBingoPlayers(this.gameId).subscribe((res) => {
        this.players = res;
        this.fetching = false;
      }, (er) => {
        this.fetching = false;
      });

    }, (err) => {
      this.fetching = false;
    }
    );
  }

  sendEmail(playerId: string) {
    //  this.fetching = true;
    this.emailSendPlayerIdentifier = undefined;

    this.bingoService.sendEmail(this.gameId, playerId).subscribe((r) => {
      const player = this.players.filter(p => p.id === playerId)[0];

      if (r === false) {
        this.emailSendPlayerIdentifier = (player.email) ? player.email : player.name;
        // this.fetching = false;
      } else {
        player.bingoSlipEmailStatus = 'SENT';
      }
    }, (err) => {
      // this.fetching = false;
    });
  }

}
