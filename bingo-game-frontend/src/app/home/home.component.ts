import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { BingoService } from '../bingo.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class HomeComponent implements OnInit {

  constructor(private bingoService: BingoService, private route: ActivatedRoute, private router: Router) {
    this.bingoService.clearLocalStorage();
  }

  ngOnInit(): void {
  }

  initiateGame() {
    this.router.navigate(['game', 'newGame']);
  }

}
