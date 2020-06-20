import { Component, OnInit } from '@angular/core';
import { BingoService } from '../bingo.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

  gameName;

  constructor(private bingoService: BingoService) {
    this.gameName = this.bingoService.getGameName();
  }

  ngOnInit(): void {
  }

  getGameName() {
    return this.gameName ? this.gameName : 'Bingo Multiplayer Game';
  }

}
