
import { Component, OnInit, ChangeDetectorRef, NgZone ,AfterViewInit, ViewChild, Type } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { HttpClientModule, HttpClient, HttpRequest, HttpResponse, HttpEventType } from '@angular/common/http';
// import { ImageCropperComponent, CropperSettings, Bounds } from 'ng2-img-cropper';
import { CdtaService } from '../../cdta.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ElectronService } from 'ngx-electron';
// import { Game, User, Card } from '../index/Game';
import { Cdta, User, Card } from '../../components/models/Cdta'
import { Observable } from 'rxjs';
declare var $: any;
declare var WebCamera: any;
declare var dialog: any;
declare var fs: any;
declare var electron: any;
declare var pcsc: any;
var pcs = pcsc();

//  electron.ipcRenderer.on('updateResult', (event, data)=>{
//     var value = data;
//     console.log('java result', value)
//   });

pcs.on('reader', function (reader) {

    console.log('New reader detected', reader.name);

    reader.on('error', function (err) {
        console.log('Error(', this.name, '):', err.message);
    });

    reader.on('status', function (status) {
        console.log('Status(', this.name, '):', status);
        /* check what has changed */
        const changes = this.state ^ status.state;
        if (changes) {
            if ((changes & this.SCARD_STATE_EMPTY) && (status.state & this.SCARD_STATE_EMPTY)) {
                console.log("card removed");/* card removed */
                reader.disconnect(reader.SCARD_LEAVE_CARD, function (err) {
                    if (err) {
                        console.log(err);
                    } else {
                        console.log('Disconnected');
                    }
                });
            } else if ((changes & this.SCARD_STATE_PRESENT) && (status.state & this.SCARD_STATE_PRESENT)) {
                console.log("card inserted");/* card inserted */
                reader.connect({ share_mode: this.SCARD_SHARE_SHARED }, function (err, protocol) {
                    if (err) {
                        console.log(err);
                    } else {
                        console.log('Protocol(', reader.name, '):', protocol);
                        reader.transmit(new Buffer([0x00, 0xB0, 0x00, 0x00, 0x20]), 40, protocol, function (err, data) {
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

    reader.on('end', function () {
        console.log('Reader', this.name, 'removed');
    });
});

pcs.on('error', function (err) {
    console.log('PCSC error', err.message);
});

@Component({
    selector: 'app-readcard',
    templateUrl: './readcard.component.html',
    styleUrls: ['./readcard.component.css']
})
export class ReadcardComponent implements OnInit {

    // Global Declarations will go here

    title = 'Add Cdta';
    url = '';
    event = "20+20";
    value: any
    public singleImage: any
    public carddata= [];
    public show: Boolean = false;

    constructor(private cdtaservice: CdtaService,  private router: Router, private _ngZone: NgZone, private electronService: ElectronService, private ref: ChangeDetectorRef, private http: HttpClient) {

        this.electronService.ipcRenderer.on('updateResult', (event, data) => {
            if (data != undefined && data != "") {
                this.show = true;
    
                this._ngZone.run(() => {
                    this.carddata = new Array(JSON.parse(data));
                });
            }  
        });

    }

    /* JAVA SERVICE CALL */

    readCard(event) {
        this.electronService.ipcRenderer.send('readSmartcard', this.event)
        console.log('java call', this.event)
    }



    ngOnInit() {
       
    }

}