import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { CreateComponent } from './components/create/create.component';
import { EditComponent } from './components/edit/edit.component';
import { IndexComponent } from './components/index/index.component';
import {HttpClientModule} from '@angular/common/http';
import { GameService } from './game.service';
import { ReactiveFormsModule } from '@angular/forms';
// import {WebCamera} from 'webcamjs';
// import {remote, dialog} from 'electron';
// import * as fs from 'fs';
import {NgxElectronModule} from 'ngx-electron';

const routes:Routes = [
  { 
    path: '', 
    redirectTo: '/create', 
    pathMatch: 'full'
   },
  { 
    path: 'create', 
    component: CreateComponent 
  },
  {
    path: 'edit/:id',
    component: EditComponent
  },
  { 
    path: 'index',
    component: IndexComponent
  }
]
@NgModule({
  declarations: [
    AppComponent,
    CreateComponent,
    EditComponent,
    IndexComponent
  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(routes),
    HttpClientModule,
    ReactiveFormsModule,
    NgxElectronModule
  ],
  providers: [GameService],
  bootstrap: [AppComponent]
})


export class AppModule { }
