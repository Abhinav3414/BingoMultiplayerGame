import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { PlayerResponse } from '../models';
import { BingoService } from '../bingo.service';

@Component({
  selector: 'app-welcome',
  templateUrl: './welcome.component.html',
  styleUrls: ['./welcome.component.scss']
})
export class WelcomeComponent implements OnInit {

  @Input() gameId: string;
  @Output() leaderAssigned = new EventEmitter<boolean>();
  leader: PlayerResponse;
  closeResult: string;

  leaderForm = new FormGroup({
    email: new FormControl('', [
      Validators.required,
      Validators.pattern('^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$')]),
    name: new FormControl('', [
      Validators.required
    ]),
    agreeTerms: new FormControl('', [
      Validators.requiredTrue
    ])
  });

  constructor(public bingoService: BingoService, private modalService: NgbModal) { }

  ngOnInit(): void {
  }

  onSubmit() {
    this.leader = new PlayerResponse(this.leaderForm.value.name, this.leaderForm.value.email);
    this.bingoService.assignLeader(this.gameId, this.leader).subscribe((r) => {
      this.leaderAssigned.emit(true);
      this.bingoService.setLeader(r);
    });
  }

  open(content) {
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

}
