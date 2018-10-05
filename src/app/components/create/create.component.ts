
import { Component, OnInit } from '@angular/core';
import { FormGroup,  FormBuilder,  Validators } from '@angular/forms';
import { GameService } from '../../game.service';
import { ActivatedRoute, Router } from '@angular/router';
declare var WebCamera: any;
declare var dialog: any;
declare var fs: any;
@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css']
})
export class CreateComponent implements OnInit {

 title = 'Add Game';

  angForm: FormGroup;
  
  enabled = false

  constructor(private gameservice: GameService, private fb: FormBuilder,private router: Router,) {
    this.createForm();
   }

   createForm() {
    this.angForm = this.fb.group({
      name: ['', Validators.required ],
      price: ['', Validators.required ]
   });
  }

  addGame(name, price) {
      this.gameservice.addGame(name, price);
  }
  goTo(){
    this.router.navigate(['index']);
  }

  captureCamera(){
    if(!this.enabled){
      this.enabled = true;
      WebCamera.attach('#camdemo');
      console.log("The camera has been started");
  }else{
      this.enabled = false;
      WebCamera.reset();
      console.log("The camera has been disabled");
  }
  }

  public processBase64Image(dataString) {
    var matches = dataString.match(/^data:([A-Za-z-+\/]+);base64,(.+)$/),response = <any> {};
    if (matches.length !== 3) {
        return new Error('Invalid input string');
    }
    response.type = matches[1];
    response.data = new Buffer(matches[2], 'base64');
    return response;
  }

  saveImage(){
    if(this.enabled){
      WebCamera.snap(function(data_uri) {
          var imageBuffer = data_uri.match(/^data:([A-Za-z-+\/]+);base64,(.+)$/),response = <any> {};
          if (imageBuffer.length !== 3) {
            return new Error('Invalid input string');
        }else{
          response.type = imageBuffer[1];
          response.data = new Buffer(imageBuffer[2], 'base64');
        }
          var savePath = dialog.showSaveDialog({
              filters: [
                   { name: 'Images', extensions: ['png'] },
              ]
          });
          fs.writeFile(savePath, response.data, function(err) {
                     if(err){
                         console.log("Cannot save the file :'( time to cry !");
                     }else{
                         alert("Image saved succesfully");
                     }
                 });
});
  }else{
      console.log("Please enable the camera first to take the snapshot !");
  }
  }

  
  ngOnInit() {
  }

}