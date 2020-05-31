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

  getHeaderWithXRequest(uId) {
    if (localStorage.getItem('leader')) {
      this.headers = this.headers.set('X-Requested-With', uId);
    }
    return this.headers;
  }

  setLeader(leader) {
    localStorage.setItem('leader', JSON.stringify(leader));
  }

  getLeader(): any {
    if (localStorage.getItem('leader')) {
      return JSON.parse(localStorage.getItem('leader'));
    }
    return undefined;
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
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.get(this.appUrl + '/' + gameId + '/getBingoPlayers', { headers: this.headers });
  }

  getUserSlips(gameId: string, playerId: string): Observable<any> {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.get(this.appUrl + '/' + gameId + '/playerslips/' + playerId, { headers: this.headers });
  }

  addPlayers(gameId: string, players): any {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.post(this.appUrl + '/' + gameId + '/gamesetup/addPlayers', players, { headers: this.headers });
  }

  uploadExcel(gameId: string, file: File): any {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    this.headers.set('Content-Type', 'multipart/form-data');
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(this.appUrl + '/' + gameId + '/gamesetup/uploadExcelFile', formData, { headers: this.headers });
  }

  downloadSlipPdf(gameId: string, email: any): any {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.get<any[]>(this.appUrl + '/download/' + gameId + '/' + email,
      { observe: 'response', responseType: 'blob' as 'json', headers: reqHeader });
  }

  callNext(gameId: string) {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.post(this.appUrl + '/' + gameId + '/callNext', null, { headers: this.headers });
  }

  getAllCalls(gameId: string): Observable<any> {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.get(this.appUrl + '/' + gameId + '/getallcalls', { headers: this.headers });
  }

}
