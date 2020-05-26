import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import { BingoService } from '../bingo.service';

@Component({
  selector: 'app-manage-player',
  templateUrl: './manage-player.component.html',
  styleUrls: ['./manage-player.component.scss']
})
export class ManagePlayerComponent implements OnInit {
  constructor(private bingoService: BingoService, private elem: ElementRef) { }
  href = 'https://www.google.com';

  @Input()
  gameId: string = null;
  players: any;

  @ViewChild('fileInput') fileInput: ElementRef;

  ngOnInit(): void {
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

  upload() {
    const file = this.fileInput.nativeElement.files[0];
    this.bingoService.uploadExcel(this.gameId, file).subscribe(
      res => {
        this.bingoService.getBingoPlayers(this.gameId).subscribe((r) => {
          this.players = r;
        });
      });
  }

}
