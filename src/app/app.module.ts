import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { CameraComponent } from './components/camera/camera.component';
import {HttpClientModule} from '@angular/common/http';
import { CdtaService } from './cdta.service';
import { ReactiveFormsModule } from '@angular/forms';
import {ImageCropperComponent} from 'ng2-img-cropper';
import {NgxElectronModule} from 'ngx-electron';
import { ReadcardComponent } from './components/readcard/readcard.component';

const routes:Routes = [
  { 
    path: '', 
    redirectTo: '/readcard', 
    pathMatch: 'full'
   },
  { 
    path: 'create', 
    component: CameraComponent 
  },

  { 
    path: 'readcard',
    component: ReadcardComponent
  }

]
@NgModule({
  declarations: [
    AppComponent,
    CameraComponent,
    ImageCropperComponent,
    ReadcardComponent
  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(routes),
    HttpClientModule,
    ReactiveFormsModule,
    NgxElectronModule,
    
  ],
  providers: [CdtaService],
  bootstrap: [AppComponent]
})


export class AppModule { }
