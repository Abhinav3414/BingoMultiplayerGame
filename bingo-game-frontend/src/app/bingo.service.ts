import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerResponse } from './models';

@Injectable({
  providedIn: 'root'
})
export class BingoService {

  headers = new HttpHeaders();
  private appUrl = 'http://localhost:8080/bingo-game';

  constructor(private http: HttpClient) { }

  initiateGame(): any {
    return this.http.post<string>(this.appUrl + '/initiategame', null);
  }

  assignLeader(gameId: string, leader: PlayerResponse) {
    return this.http.post(this.appUrl + '/' + gameId + '/assignLeader', leader);
  }

  getGameSetupStatus(gameId: string) {
    return this.http.get(this.appUrl + '/' + gameId + '/gameSetupStatus');
  }

  getSampleExcel(): Observable<Blob> {
    return this.http.get(this.appUrl + '/sampleexcel', { responseType: 'blob' });
  }

  getBingoPlayers(gameId: string): Observable<any> {
    return this.http.get(this.appUrl + '/' + gameId + '/getBingoPlayers');
  }

  getUserSlips(gameId: string, email: string): Observable<any> {
    return this.http.get(this.appUrl + '/' + gameId + '/playerslips/' + email);
  }

  addPlayers(gameId: string, players): any {
    return this.http.post(this.appUrl + '/' + gameId + '/gamesetup/addPlayers', players);
  }

  uploadExcel(gameId: string, file: File): any {
    this.headers.set('Content-Type', 'multipart/form-data');
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(this.appUrl + '/' + gameId + '/gamesetup/uploadExcelFile', formData, { headers: this.headers });
  }

  callNext(gameId: string) {
    return this.http.post(this.appUrl + '/' + gameId + '/callNext', null);
  }

}
