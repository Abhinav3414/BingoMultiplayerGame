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
  @Input() playerId: null;
  @Input() gameId: null;
  @Input() playerEmail: null;
  slipResponse: any;
  closeResult: string;

  constructor(private modalService: NgbModal, private bingoService: BingoService) { }

  ngOnInit(): void {
  }

  open(content) {
    this.bingoService.getUserSlips(this.gameId, this.playerId).subscribe((r) => {
      this.slipResponse = r;
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

  downloadSlipPdf() {

    this.bingoService.downloadSlipPdf(this.gameId, this.playerEmail).subscribe((res: HttpResponse<Blob>) => {

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