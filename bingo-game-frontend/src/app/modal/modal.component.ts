import { Component, OnInit, Input } from '@angular/core';
import { NgbModal, ModalDismissReasons } from '@ng-bootstrap/ng-bootstrap';
import { BingoService } from '../bingo.service';
import { HttpResponse } from '@angular/common/http';

@Component({
  selector: 'app-modal',
  templateUrl: './modal.component.html',
  styleUrls: ['./modal.component.scss']
})
export class ModalComponent implements OnInit {
  @Input() player: any;
  @Input() gameId: null;
  slipResponse: any;
  closeResult: string;

  showPlayerId = false;
  is75Board = false;

  constructor(private modalService: NgbModal, private bingoService: BingoService) { }

  ngOnInit(): void {
  }

  open(content) {
    this.showPlayerId = false;

    this.bingoService.getUserSlips(this.gameId, this.player.id).subscribe((r) => {
      this.slipResponse = r;
      if (this.slipResponse.responses[0].transformedMatrix[0].length === 5) {
        this.is75Board = true;
      }
    });

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

  copyMessage(val: string) {
    const selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = val;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
  }

  showId() {
    this.showPlayerId = !this.showPlayerId;
  }

  downloadSlipPdf() {

    this.bingoService.downloadSlipPdf(this.gameId, this.player.id).subscribe((res: HttpResponse<Blob>) => {

      let fileName = 'bingo_slips.pdf';

      if (res.headers && res.headers.get('content-disposition')) {
        const str = res.headers.get('content-disposition');
        fileName = str.substring(str.indexOf('=') + 1, str.length);
      }
      const file = new Blob([res.body], { type: 'application/pdf' });

      const link = document.createElement('a');
      link.href = window.URL.createObjectURL(file);
      link.download = fileName;
      link.target = '_blank';
      link.click();
      window.URL.revokeObjectURL(link.href);

    });
  }

}
