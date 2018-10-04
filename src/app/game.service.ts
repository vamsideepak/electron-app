import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Game } from './components/index/Game';

@Injectable({
  providedIn: 'root'
})
export class GameService {

    

 	constructor(private http: HttpClient) { }
   public uri = 'http://192.168.1.31:8080/encoder';
  	addGame(name, price) {
        const obj = {
          name: name,
          price: price
        };
        this
        	.http
        	.post(`${this.uri}/games/add`, obj)
            .subscribe(res => 
            	console.log('Done')
            );
  	}

  	getGames() {
             return this
                    .http
                    .get(`${this.uri}/games`);
      }


      cardData() {
        return this
               .http
               .get(`${this.uri}`);
 }

      editGame(id) {
        return this
              .http
              .get(`${this.uri}/games/edit/${id}`)
        }

        updateGame(name, price, id) {

          const obj = {
          name: name,
           price: price
           };
        return   this
           .http
           .post(`${this.uri}/games/update/${id}`, obj)
           
        }

        deleteGame(id) {
          return this
                .http
                .get(`${this.uri}/games/delete/${id}`)
        }
 }


