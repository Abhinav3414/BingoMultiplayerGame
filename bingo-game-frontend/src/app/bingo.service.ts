import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlayerResponse } from './models';

@Injectable({
  providedIn: 'root'
})
export class BingoService {

  baseUrl: string;
  headers = new HttpHeaders();
  appUrl;

  constructor(private http: HttpClient) {
    this.baseUrl = (location.origin === 'http://localhost:4200') ? 'http://localhost:8080' : location.origin;
    this.appUrl = this.baseUrl + '/bingo-game';
  }

  setLeader(leader) {
    localStorage.setItem('leader', JSON.stringify(leader));
  }

  getLeader(): any {
    const user = JSON.parse(localStorage.getItem('leader'));
    return user;
  }

  initiateGame(): any {
    localStorage.removeItem('leader');
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

  getUserSlips(gameId: string, playerId: string): Observable<any> {
    return this.http.get(this.appUrl + '/' + gameId + '/playerslips/' + playerId);
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

  downloadSlipPdf(gameId: string, email: any): any {
    return this.http.get<any[]>(this.appUrl + '/download/' + gameId + '/' + email,
      { observe: 'response', responseType: 'blob' as 'json' });
  }

  callNext(gameId: string) {
    return this.http.post(this.appUrl + '/' + gameId + '/callNext', null);
  }

  getAllCalls(gameId: string): Observable<any> {
    return this.http.get(this.appUrl + '/' + gameId + '/getallcalls');
  }

}
