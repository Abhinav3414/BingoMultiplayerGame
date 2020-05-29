import { Component, OnInit } from '@angular/core';
import { BingoService } from '../bingo.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  constructor(private bingoService: BingoService, private route: ActivatedRoute, private router: Router) { }

  ngOnInit(): void {
  }

  initiateGame() {
    this.bingoService.initiateGame().subscribe(
      res => {
        this.router.navigate(['/game', res.gameId]);
      }
    );
  }

}
