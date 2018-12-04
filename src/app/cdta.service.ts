import { Injectable } from '@angular/core';
//import { Game } from './components/index/Game';
import { Observable, of, throwError } from 'rxjs';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { catchError, tap, map } from 'rxjs/operators';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

const apiUrl = 'https://api.qa.gfcp.io/services/data-api/v1/wpf/id_card/updateExisting?tenant=CDTA&access_token=94ccd96b-ff75-4b9c-b12d-a3257b1ebde4';

@Injectable({
  providedIn: 'root'
})



export class CdtaService {



  constructor(private http: HttpClient) { }


  

  addGame(name, price) {
    const obj = {
      name: name,
      price: price
    };
    this
      .http
      .post(`${apiUrl}/games/add`, obj)
      .subscribe(res =>
        console.log('Done')
      );
  }


  /** upload image api */

  // uploadImage(image) {
  //   return this
  //     .http
  //     .post(`${apiUrl}`, image)

  // }


  uploadImage(data): Observable<any> {
    return this.http.post(apiUrl, data, httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  // getGames() {
  //   return this
  //     .http
  //     .get(`${apiUrl}/games`);
  // }


  cardData() {
    return this
      .http
      .get(`${apiUrl}`);
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${error.error}`);
    }
    // return an observable with a user-facing error message
    return throwError('Something bad happened; please try again later.');
  };

  private extractData(res: Response) {
    let body = res;
    return body || { };
  }
}


