import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

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

  getSampleExcel(): Observable<Blob> {
    return this.http.get(this.appUrl + '/sampleexcel', { responseType: 'blob' });
  }

  getBingoPlayers(gameId): Observable<any> {
    return this.http.get(this.appUrl + '/' + gameId + '/getBingoPlayers');
  }

  getUserSlips(gameId, email): Observable<any> {
    return this.http.get(this.appUrl + '/' + gameId + '/playerslips/' + email);
  }

  addPlayers(gameId, players): any {
    return this.http.post(this.appUrl + '/' + gameId + '/gamesetup/addPlayers', players);
  }

  uploadExcel(gameId, file: File): any {
    this.headers.set('Content-Type', 'multipart/form-data');
    const formData: FormData = new FormData();
    formData.append('file', file);
    return this.http.post(this.appUrl + '/' + gameId + '/gamesetup/uploadExcelFile', formData, { headers: this.headers });
  }

}
