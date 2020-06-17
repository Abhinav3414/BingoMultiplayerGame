import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ServiceWorkerModule } from '@angular/service-worker';
import { environment } from '../environments/environment';

import { SetupComponent } from './setup/setup.component';
import { FooterComponent } from './footer/footer.component';
import { GameRoomComponent } from './game-room/game-room.component';
import { HomeComponent } from './home/home.component';
import { ManagePlayerComponent } from './manage-player/manage-player.component';
import { CallNumberComponent } from './call-number/call-number.component';

import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ModalComponent } from './modal/modal.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { HashLocationStrategy, LocationStrategy } from '@angular/common';

import { ShareIconsModule } from 'ngx-sharebuttons/icons';
import { ShareButtonsConfig } from 'ngx-sharebuttons';
import { ShareButtonsModule } from 'ngx-sharebuttons/buttons';
import { HeaderComponent } from './header/header.component';
import { WelcomeComponent } from './welcome/welcome.component';

const customConfig: ShareButtonsConfig = {
  include: ['twitter', 'whatsapp', 'telegram', 'copy'],
  // exclude: ['tumblr', 'stumble', 'vk', 'print' ,'sms', 'facebook', 'linkedin'],
  theme: 'modern-light',
  gaTracking: true,
  windowWidth: 500,
  windowHeight: 500
  // twitterAccount: 'twitterUsername'
  // prop: {
  //   facebook: {
  //     icon: ['fab', 'fa-facebook-official'],
  //     text: 'Share'
  //   },
  //   twitter: {
  //     icon: ['fab', 'fa-twitter-square'],
  //     text: 'Tweet'
  //   }
  // }
};

@NgModule({
  declarations: [
    AppComponent,
    SetupComponent,
    FooterComponent,
    HomeComponent,
    ManagePlayerComponent,
    CallNumberComponent,
    ModalComponent,
    GameRoomComponent,
    HeaderComponent,
    WelcomeComponent
  ],
  imports: [
    HttpClientModule,
    BrowserModule,
    AppRoutingModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    ShareIconsModule,
    ShareButtonsModule.withConfig(customConfig),
    ServiceWorkerModule.register('ngsw-worker.js', { enabled: environment.production })
  ],
  providers: [{ provide: LocationStrategy, useClass: HashLocationStrategy }],
  bootstrap: [AppComponent]
})
export class AppModule { }
