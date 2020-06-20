import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
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

  setGameName(gameName: string) {
    localStorage.setItem('gameName', JSON.stringify(gameName));
  }

  getGameName(): any {
    if (localStorage.getItem('gameName')) {
      return JSON.parse(localStorage.getItem('gameName'));
    }
    return undefined;
  }

  clearLocalStorage() {
    localStorage.removeItem('leader');
    localStorage.removeItem('gameName');
  }

  assignLeader(leader: PlayerResponse) {
    return this.http.post(this.appUrl + '/assignLeader', leader);
  }

  setUpBoardTypeAndSlipCount(gameId: string, boardType: string, slips: number, emailSlips: any, gameName: string) {

    const params = new HttpParams();
    params.set('emailSlips', emailSlips);
    params.set('gameName', gameName);

    return this.http.post(this.appUrl + '/' + gameId +
      '/boardType/' + boardType + '/slipcount/' + slips + '?emailSlips=' + emailSlips + '&gameName=' + gameName, null, { params });
  }

  enterGameRoom(gameId: string) {
    const leaderEmail = this.getLeader() ? this.getLeader().email : undefined;
    return this.http.post(this.appUrl + '/' + gameId + '/entergameroom/' + leaderEmail, null);
  }

  getGameSetupStatus(gameId: string) {
    return this.http.get(this.appUrl + '/' + gameId + '/gameSetupStatus');
  }

  sendEmailToAll(gameId: string) {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.post(this.appUrl + '/' + gameId + '/sendEmailToAll', null, { headers: this.headers });
  }

  sendEmail(gameId: string, playerId: string) {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.post(this.appUrl + '/' + gameId + '/sendEmail/' + playerId, null, { headers: this.headers });
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

  downloadSlipPdf(gameId: string, playerId: any): any {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.get<any[]>(this.appUrl + '/download/' + gameId + '/' + playerId,
      { observe: 'response', responseType: 'blob' as 'json', headers: reqHeader });
  }

  callNext(gameId: string) {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.post(this.appUrl + '/' + gameId + '/callNext', null, { headers: this.headers });
  }

  getAllCalls(gameId: string): Observable<any> {
    return this.http.get(this.appUrl + '/' + gameId + '/getallcalls');
  }

  startCalls(gameId: string) {
    const reqHeader = (this.getLeader()) ? this.getHeaderWithXRequest(this.getLeader().id) : this.headers;
    return this.http.post(this.appUrl + '/' + gameId + '/startCalls', null, { headers: this.headers });
  }

  updateSlipNumber(gameId: string, playerId: any, slipRow: any, slipCol: any, slipId: any): any {
    const slipInfo = {
      row: slipRow,
      col: slipCol,
      id: slipId
    };
    return this.http.post(this.appUrl + '/' + gameId + '/updateSlip/' + playerId, slipInfo);
  }

}
