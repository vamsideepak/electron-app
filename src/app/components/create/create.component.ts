
import { Component, OnInit } from '@angular/core';
import { FormGroup,  FormBuilder,  Validators } from '@angular/forms';
import { GameService } from '../../game.service';
import { ActivatedRoute, Router } from '@angular/router';
import {ElectronService} from 'ngx-electron';
//import { NFC } from 'nfc-pcsc';
//import {pcsc} from 'pcsclite';
declare var WebCamera: any;
declare var dialog: any;
declare var fs: any;
//declare var NFC: any;
declare var electron: any;
 declare var pcsc: any;
 var pcs = pcsc();

//  electron.ipcRenderer.on('updateResult', (event, data)=>{
//     var value = data;
//     console.log('java result', value)
//   });

pcs.on('reader', function(reader) {

  console.log('New reader detected', reader.name);

  reader.on('error', function(err) {
      console.log('Error(', this.name, '):', err.message);
  });

  reader.on('status', function(status) {
      console.log('Status(', this.name, '):', status);
      /* check what has changed */
      const changes = this.state ^ status.state;
      if (changes) {
          if ((changes & this.SCARD_STATE_EMPTY) && (status.state & this.SCARD_STATE_EMPTY)) {
              console.log("card removed");/* card removed */
              reader.disconnect(reader.SCARD_LEAVE_CARD, function(err) {
                  if (err) {
                      console.log(err);
                  } else {
                      console.log('Disconnected');
                  }
              });
          } else if ((changes & this.SCARD_STATE_PRESENT) && (status.state & this.SCARD_STATE_PRESENT)) {
              console.log("card inserted");/* card inserted */
              reader.connect({ share_mode : this.SCARD_SHARE_SHARED }, function(err, protocol) {
                  if (err) {
                      console.log(err);
                  } else {
                      console.log('Protocol(', reader.name, '):', protocol);
                      reader.transmit(new Buffer([0x00, 0xB0, 0x00, 0x00, 0x20]), 40, protocol, function(err, data) {
                          if (err) {
                              console.log(err);
                          } else {
                              console.log('Data received', data);
                              console.log('Data base64', data.toString('base64'));
                              // reader.close();
                              // pcs.close();
                          }
                      });
                  }
              });
          }
      }
  });

  reader.on('end', function() {
      console.log('Reader',  this.name, 'removed');
  });
});

pcs.on('error', function(err) {
  console.log('PCSC error', err.message);
});

@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.css']
})
export class CreateComponent implements OnInit {

 title = 'Add Game';

  angForm: FormGroup;
  event = "20+20";
  enabled = false
value:any
  constructor(private gameservice: GameService, private fb: FormBuilder,private router: Router, private electronService: ElectronService) {
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

//JAVA CALL
  fromJava(){
this.electronService.ipcRenderer.send('readSmartcard',this.event)
console.log('java call', this.event)
// this.electronService.ipcRenderer.on('updateResult', function(event, data){
//     this.value = data;
//     console.log('java result', this.value)
//   });
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

//smart card code 


  
  ngOnInit() {
    this.electronService.ipcRenderer.on('updateResult', (event, data)=>{
        this.value = data;
        console.log('final result', this.value)
      });
  }

}