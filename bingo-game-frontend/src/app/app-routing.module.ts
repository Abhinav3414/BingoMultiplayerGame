import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { SetupComponent } from './setup/setup.component';
import { HomeComponent } from './home/home.component';
import { GameSetupResolver } from './setup/game.setup.resolver';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'game/:gameId', component: SetupComponent, resolve: { gameSetupStatus: GameSetupResolver } }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
