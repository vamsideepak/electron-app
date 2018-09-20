import { Component, OnInit } from '@angular/core';
import { Game, User } from './Game';
import { GameService } from '../../game.service';
import { ActivatedRoute, Router } from '@angular/router';
@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css']
})
export class IndexComponent implements OnInit {

    games: Game[];
 	constructor(private gameservice: GameService, private router: Router) { }

  	ngOnInit() {
  		this.gameservice
      		.getGames()
      		.subscribe((data: User[]) => {
        		this.games = data;
        });
    }

    deleteGame(id) {
      this.gameservice.deleteGame(id).subscribe(res => {
       console.log('Deleted');
     });
    }
    backToCreate(){
      this.router.navigate(['create']);
    }

}
