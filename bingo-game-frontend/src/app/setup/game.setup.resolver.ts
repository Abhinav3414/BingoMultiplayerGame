import { Injectable } from '@angular/core';
import { Resolve, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { BingoService } from '../bingo.service';

@Injectable({ providedIn: 'root' })
export class GameSetupResolver implements Resolve<any> {
  constructor(private bingoService: BingoService) {
  }

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> {

    const gameId = route.paramMap.get('gameId');
    if (gameId !== 'newGame') {
      return this.bingoService.getGameSetupStatus(route.paramMap.get('gameId'));
    } else {
      return null;
    }

  }

}
