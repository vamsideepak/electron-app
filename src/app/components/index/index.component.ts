import { Component, OnInit } from '@angular/core';
import { Game, User } from './Game';
import { GameService } from '../../game.service';
import { ActivatedRoute, Router } from '@angular/router';
import {ElectronService} from 'ngx-electron';
//import { app, BrowserWindow, ipcMain, IpcMessageEvent } from 'electron';
@Component({
  selector: 'app-index',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.css']
})
export class IndexComponent implements OnInit {

    games: Game[];
 	constructor(private gameservice: GameService, private router: Router, private _electronService: ElectronService) { }

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
     playPingPong() {
      if(this._electronService.isElectronApp) {
         // let pong: string = this._electronService.ipcRenderer.sendSync('ping');
          //console.log(pong);
         // launchWindow() {
            this._electronService.shell.openExternal('https://electronjs.org');
         // }
      }
    }

}
